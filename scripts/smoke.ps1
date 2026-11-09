param([string]$BaseUrl='http://127.0.0.1:5050/api', [switch]$LiveAi)
$ErrorActionPreference='Stop'
if ($BaseUrl -notmatch '^http://(127\.0\.0\.1|localhost):\d+/api$') { throw 'This smoke test is restricted to localhost.' }
$checks = [System.Collections.Generic.List[object]]::new()
function Check($name, $condition) {
    $checks.Add([pscustomobject]@{name=$name;passed=[bool]$condition})
    if (-not $condition) { throw "FAILED: $name" }
    Write-Host "PASS $name"
}
function Api($path, $method='GET', $data=$null, $token=$null, [switch]$Json) {
    $args=@{Uri="$BaseUrl/$path";Method=$method;TimeoutSec=35}
    if($token){$args.Headers=@{Authorization="Bearer $token"}}
    if($null -ne $data){
        if($Json){$args.ContentType='application/json; charset=utf-8';$args.Body=[System.Text.Encoding]::UTF8.GetBytes(($data|ConvertTo-Json -Depth 10 -Compress))}
        else{$args.ContentType='application/x-www-form-urlencoded; charset=utf-8';$args.Body=$data}
    }
    Invoke-RestMethod @args
}
$user='demo_'+[DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$password='WhereMoneyDemo!2026'
Check 'register' ((Api 'user/register' POST @{userName=$user;password=$password}).code -eq 200)
$token=(Api 'user/login' POST @{userName=$user;password=$password}).data.token
Check 'login' (-not [string]::IsNullOrEmpty($token))
$bookId=(Api 'book/all' GET -token $token).data.bookList[0].id
foreach($name in @('支付宝','招商银行卡')){
    Check "create asset $name" ((Api 'asset/asset' POST @{assetName=$name;balance=10000;type='资金';inTotal='true';svg='ic:baseline-account-balance-wallet'} $token).code -eq 200)
}
$context=(Api "ai/context?bookId=$bookId&type=$([uri]::EscapeDataString('支出'))" GET -token $token).data
$asset=$context.assets|Where-Object assetName -eq '支付宝'|Select-Object -First 1
$other=$context.assets|Where-Object assetName -eq '招商银行卡'|Select-Object -First 1
$category=$context.categories|Where-Object billCategoryName -match '餐|饮食'|Select-Object -First 1
if(-not $category){$category=$context.categories[0]}
Check 'set total budget' ((Api 'budget/budget/total' PATCH @{bookId=$bookId;totalBudget=1000} $token).code -eq 200)
Check 'set category budget' ((Api 'budget/budget' POST @{bookId=$bookId;billCategoryId=$category.id;limit=150} $token).code -eq 200)
$draft=@{requestId=[guid]::NewGuid().ToString();bookId=$bookId;type='支出';amount='128.00';transferFee=$null;billTime=(Get-Date -Format 'yyyy-MM-dd HH:mm:ss');remark='火锅联调账单';outAssetId=$asset.id;inAssetId=$null;billCategoryId=$category.id}
if($LiveAi){
    $parsed=Api 'ai/parse' POST @{bookId=$bookId;type='支出';text='今天用支付宝吃火锅花了128元'} $token -Json
    Check 'live AI parse' ($parsed.code -eq 200 -and $parsed.data.amount -eq 128 -and $parsed.data.outAssetId -eq $asset.id)
    $draft.billCategoryId=$parsed.data.billCategoryId
    $draft.billTime=$parsed.data.billTime
}
Check 'confirm draft' ((Api 'ai/confirm' POST $draft $token -Json).code -eq 200)
$repeat=Api 'ai/confirm' POST $draft $token -Json
Check 'idempotent retry' ($repeat.code -eq 200 -and $repeat.data.alreadySaved)
$balance=(Api "asset/asset?id=$($asset.id)" GET -token $token).data.asset.balance
Check 'balance debited once' ($balance -eq 9872)
$alerts=Api "ai/budget-alerts?bookId=$bookId" GET -token $token
Check 'budget warning' ($alerts.code -eq 200 -and ($alerts.data.items|Where-Object level -eq 'WARNING').Count -gt 0)
$bad=$draft.Clone();$bad.requestId=[guid]::NewGuid().ToString();$bad.amount='-1'
Check 'negative amount rejected' ((Api 'ai/confirm' POST $bad $token -Json).code -eq 422)
$user2=$user+'_other'
$null=Api 'user/register' POST @{userName=$user2;password=$password}
$token2=(Api 'user/login' POST @{userName=$user2;password=$password}).data.token
Check 'cross-user asset denied' ((Api "asset/asset?id=$($asset.id)" GET -token $token2).code -eq 403)
Check 'cross-user book denied' ((Api "bill/all?bookId=$bookId" GET -token $token2).code -eq 403)
Check 'unauthenticated denied' ((Api 'book/all').code -eq 401)
if($LiveAi){
    $monthly=Api 'ai/monthly-report' POST @{bookId=$bookId;month=(Get-Date -Format 'yyyy-MM')} $token -Json
    Check 'report factual totals' ($monthly.code -eq 200 -and $monthly.data.facts.current.netExpense -eq 128)
    Check 'live AI report' ($monthly.data.source -eq 'AI')
    $cached=Api 'ai/monthly-report' POST @{bookId=$bookId;month=(Get-Date -Format 'yyyy-MM')} $token -Json
    Check 'report cache hit' $cached.data.cached
}
[pscustomobject]@{userName=$user;password=$password;bookId=$bookId;checks=$checks;liveAi=[bool]$LiveAi} | ConvertTo-Json -Depth 8
