name: "SonarQube Full Scan"
description: "Performs full SonarQube analysis (prepare + post-build) for .NET projects"

inputs:
  sonarqube_url:
    description: "SonarQube server URL"
    required: true
  sonarqube_token:
    description: "SonarQube token"
    required: true
  SonarQubeProjectName:
    description: "SonarQube project name"
    required: true
  SonarQubeExclusions:
    description: "SonarQube exclusions"
    required: false
    default: "**/bin/**,**/obj/**"
  SonarQubeCoverletReportPaths:
    description: "Paths to Coverlet coverage report(s)"
    required: false
    default: ""
  SolutionFile:
    description: "Path to the .sln file"
    required: true
  BuildArgs:
    description: "Extra arguments for the dotnet build command"
    required: false
    default: ""
  SonarprojectBaseDir:
    description: "Optional project base directory"
    required: false
    default: "."

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

    - name: Install dotnet-sonarscanner
      shell: powershell
      run: |
        dotnet tool install --global dotnet-sonarscanner
        $env:PATH += ";$env:USERPROFILE\.dotnet\tools"

    - name: SonarQube Begin Analysis
      shell: powershell
      run: |
        dotnet sonarscanner begin `
          /k:"${{ inputs.SonarQubeProjectName }}" `
          /d:sonar.host.url="${{ inputs.sonarqube_url }}" `
          /d:sonar.login="${{ inputs.sonarqube_token }}" `
          /d:sonar.exclusions="${{ inputs.SonarQubeExclusions }}" `
          /d:sonar.test.exclusions="${{ inputs.SonarQubeExclusions }}" `
          /d:sonar.coverage.exclusions="**/*Tests*.cs,**/Models/**/*,**/Data/**/*,**/Program.cs,**/Startup.cs,${{ inputs.SonarQubeExclusions }}" `
          /d:sonar.cs.roslyn.ignoreIssues="false" `
          /d:sonar.cs.opencover.reportsPaths="${{ inputs.SonarQubeCoverletReportPaths }}"

    - name: Build .NET Project
      shell: powershell
      run: |
        dotnet build "${{ inputs.SolutionFile }}" ${{ inputs.BuildArgs }}

    - name: SonarQube End Analysis
      shell: powershell
      run: |
        dotnet sonarscanner end /d:sonar.login="${{ inputs.sonarqube_token }}"
