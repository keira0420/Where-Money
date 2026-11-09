Set-Location C:\Where-Money\Where-Money-Frontend\
git pull
npm i -f
npm run build
Remove-Item C:\Tomcat\webapps\where-money\ -Recurse
Copy-Item .\where-moeny -Recurse C:\Tomcat\webapps\where-money