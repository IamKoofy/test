name: "SonarQube Prepare"
description: "Prepares the project for SonarQube scanning"
inputs:
  sonarqube_url:
    description: "SonarQube server URL"
    required: true
  sonarqube_token:
    description: "SonarQube token"
    required: true
  project_name:
    description: "SonarQube project name"
    required: true
  exclusions:
    description: "SonarQube exclusions"
    required: false
    default: "**/bin/**,**/obj/**"
  coverage_paths:
    description: "SonarQube coverlet report paths"
    required: false
    default: ""
runs:
  using: "composite"
  steps:
    - name: Add Project GUID to .csproj Files
      shell: powershell
      run: |
        function update-project-file($file) {
            $fileContent = Get-Content -path $file.FullName
            if ($fileContent -like "*ProjectGuid*") {
                Write-Host "$($file.Name) already has a project id assigned"
                return
            }
            [xml]$xmlFile = $fileContent
            $node = $xmlFile.SelectSingleNode("//Project/PropertyGroup")
            $child = $xmlFile.CreateElement("ProjectGuid")
            $child.InnerText = "{"+[guid]::NewGuid().ToString().ToUpper()+"}"
            $node.AppendChild($child)
            $xmlFile.Save($file.FullName) | Out-Null
            Write-Host "$($file.Name) has been assigned a new project id"
        }
        Get-ChildItem -Recurse -Filter *.csproj | ForEach-Object { update-project-file $_ }

    - name: SonarQube Prepare Scan
      shell: powershell
      run: |
        dotnet tool install --global dotnet-sonarscanner
        $env:PATH += ";$env:USERPROFILE\.dotnet\tools"

        dotnet sonarscanner begin `
          /k:"${{ inputs.project_name }}" `
          /d:sonar.host.url="${{ inputs.sonarqube_url }}" `
          /d:sonar.login="${{ inputs.sonarqube_token }}" `
          /d:sonar.exclusions="${{ inputs.exclusions }}" `
          /d:sonar.test.exclusions="${{ inputs.exclusions }}" `
          /d:sonar.coverage.exclusions="**/*Tests*.cs,**/Models/**/*,**/Data/**/*,**/Program.cs,**/Startup.cs,${{ inputs.exclusions }}" `
          /d:sonar.cs.roslyn.ignoreIssues="false" `
          /d:sonar.cs.opencover.reportsPaths="${{ inputs.coverage_paths }}"
