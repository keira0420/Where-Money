param(
    [string]$JavaHome,
    [string]$MysqlBin='E:\mysql-8.0.31-winx64\bin',
    [string]$RedisServer='E:\Redis-x64-3.2.100\redis-server.exe',
    [switch]$Build
)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$runtime=Join-Path $root '.runtime'
New-Item -ItemType Directory -Force $runtime | Out-Null
if(-not $JavaHome){
    $candidates=@($env:JAVA_HOME,"$env:USERPROFILE\.jdks\graalvm-jdk-21.0.7")
    $JavaHome=$candidates|Where-Object {$_ -and (Test-Path (Join-Path $_ 'bin/java.exe'))}|Select-Object -Last 1
}
if(-not $JavaHome){throw 'Pass -JavaHome with a Java 21 JDK directory.'}
$env:JAVA_HOME=$JavaHome
$backend=Join-Path $root 'Where-Money-Backend'
$frontend=Join-Path $root 'Where-Money-Frontend'
if($Build){
    Push-Location $backend
    try{& ./mvnw.cmd -DskipTests package; if($LASTEXITCODE -ne 0){throw 'Backend build failed'}}finally{Pop-Location}
    Push-Location $frontend
    try{& npm.cmd ci --ignore-scripts; if($LASTEXITCODE -ne 0){throw 'npm ci failed'}; & npm.cmd run typecheck; if($LASTEXITCODE -ne 0){throw 'Typecheck failed'}; & npm.cmd run build; if($LASTEXITCODE -ne 0){throw 'Frontend build failed'}}finally{Pop-Location}
}
$state=@{}
function Listening($port){[bool](Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue)}
function Spawn($name,$file,$arguments,$directory){
    $process=Start-Process $file -ArgumentList $arguments -WorkingDirectory $directory -WindowStyle Hidden -PassThru -RedirectStandardOutput "$runtime/$name.out.log" -RedirectStandardError "$runtime/$name.err.log"
    $state[$name]=$process.Id
}
if(-not (Listening 3307)){
    $mysqlData=Join-Path $runtime 'mysql'
    New-Item -ItemType Directory -Force $mysqlData | Out-Null
    $baseDir=Split-Path $MysqlBin -Parent
    if(-not (Test-Path (Join-Path $mysqlData 'mysql'))){
        & "$MysqlBin/mysqld.exe" --no-defaults --initialize-insecure "--basedir=$baseDir" "--datadir=$mysqlData"
        if($LASTEXITCODE -ne 0){throw 'MySQL initialization failed'}
    }
    Spawn 'mysql' "$MysqlBin/mysqld.exe" @('--no-defaults',"--basedir=`"$baseDir`"","--datadir=`"$mysqlData`"",'--port=3307','--bind-address=127.0.0.1','--mysqlx=0','--console') $root
}
if(-not (Listening 6380)){Spawn 'redis' $RedisServer @('--port','6380','--bind','127.0.0.1','--dir',"`"$runtime`"") $root}
if(-not (Listening 5050)){
    $jar=Join-Path $backend 'target/Where-Money-Backend-1.0.jar'
    if(-not (Test-Path $jar)){throw 'Build the backend first, or use -Build.'}
    Spawn 'backend' "$JavaHome/bin/java.exe" @('-Duser.timezone=Asia/Shanghai','-jar',"`"$jar`"",'--spring.profiles.active=local') $backend
}
if(-not (Listening 13140)){
    $node=(Get-Command node.exe).Source
    Spawn 'frontend' $node @('node_modules/vite/bin/vite.js','--host','127.0.0.1') $frontend
}
$state|ConvertTo-Json|Set-Content -LiteralPath "$runtime/processes.json" -Encoding utf8
Write-Host 'Frontend: http://localhost:13140'
Write-Host 'Health:   http://localhost:5050/actuator/health'
Write-Host 'OpenAPI:  http://localhost:5050/swagger-ui/index.html'
Write-Host 'Local data is isolated in .runtime, MySQL 3307 and Redis 6380.'
