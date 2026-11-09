$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$path=Join-Path $root '.runtime/processes.json'
if(-not (Test-Path $path)){Write-Host 'No recorded local processes.';return}
$state=Get-Content -Raw $path|ConvertFrom-Json
foreach($entry in $state.PSObject.Properties){
    $process=Get-CimInstance Win32_Process -Filter "ProcessId=$($entry.Value)" -ErrorAction SilentlyContinue
    if(-not $process){continue}
    $command=$process.CommandLine
    $owned=($command -like "*$root*") -or ($entry.Name -eq 'frontend' -and $command -like '*node_modules/vite/bin/vite.js*')
    if($owned){Stop-Process -Id $entry.Value;Write-Host "Stopped $($entry.Name)"}
    else{Write-Warning "Skipped reused or unverified PID $($entry.Value)"}
}
