Set-Location C:\Where-Money\Where-Money-Backend\
git pull
Get-ScheduledTask -TaskName 'Where-Money-Backend' | Stop-ScheduledTask
mvn clean
mvn compile
mvn package
Get-ScheduledTask -TaskName 'Where-Money-Backend' | Start-ScheduledTask