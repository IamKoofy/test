dotnet tool install --global dotnet-sonarscanner
$env:PATH += ";$env:USERPROFILE\.dotnet\tools"

dotnet sonarscanner begin `
  /k:"$env:ProjectKey" `
  /d:sonar.host.url="$env:SonarQubeUrl" `
  /d:sonar.login="$env:SonarToken" `
  /d:sonar.exclusions="$env:Exclusions" `
  /d:sonar.cs.opencover.reportsPaths="$env:CoverageReport"

dotnet build "$env:SolutionFile" $env:BuildArgs

dotnet test "$env:TestProject" $env:BuildArgs `
  --filter "(TestCategory=Unit)|(TestCategory=Component)" `
  --collect "Code Coverage" `
  /p:EnableNETAnalyzers=false

dotnet sonarscanner end /d:sonar.login="$env:SonarToken"
