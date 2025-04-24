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
  PathToTestProject:
    required: true
  BuildArgs:
    description: "Extra arguments for the dotnet build command"
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

    - name: Install dotnet-sonarscanner
      shell: powershell
      run: |
        dotnet tool install --global dotnet-sonarscanner
        $env:PATH += ";$env:USERPROFILE\.dotnet\tools"

    - name: Debug Working Directory
      shell: powershell
      run: |
         pwd

    - name: SonarQube Begin Analysis
      shell: powershell
      run: |
        dotnet sonarscanner begin `
          /k:"${{ inputs.SonarQubeProjectName }}" `
          /d:sonar.host.url="${{ inputs.sonarqube_url }}" `
          /d:sonar.login="${{ inputs.sonarqube_token }}" `
          /d:sonar.exclusions="${{ inputs.SonarQubeExclusions }}" `
          /d:sonar.test.exclusions="${{ inputs.SonarQubeExclusions }}" `
          /d:sonar.coverage.exclusions="**/*Tests*.cs,**/Models/**/*,*SonarprojectBaseDir*/Data/**/*,**/Program.cs,**/Startup.cs,${{ inputs.SonarQubeExclusions }}" `
          /d:sonar.cs.roslyn.ignoreIssues="false" `
          /d:sonar.cs.opencover.reportsPaths="${{ inputs.SonarQubeCoverletReportPaths }}"

    - name: Build .NET Project
      shell: powershell
      run: |
        dotnet build "${{ inputs.SolutionFile }}" ${{ inputs.BuildArgs }}

    - name: Run Unit and Component Tests
      shell: powershell
      run: |
        dotnet test "${{ inputs.PathToTestProject }}" ${{ inputs.BuildArgs }} `
          --filter "(TestCategory=Unit)|(TestCategory=Component)" `
          --collect "Code Coverage" `
          /p:EnableNETAnalyzers=false

    - name: SonarQube End Analysis and Extract Task ID
      id: end_analysis
      shell: powershell
      run: |
        $ErrorActionPreference = "Stop"

        Write-Host "Running SonarScanner end step..."
        $sonarOutput = dotnet sonarscanner end /d:sonar.login="${{ inputs.sonarqube_token }}" 2>&1

        $sonarOutput | ForEach-Object { Write-Host $_ }

        $taskIdPattern = 'https:\/\/codequality\.gbt\.gbtad\.com\/api\/ce\/task\?id=([a-zA-Z0-9\-_]+)'
        $ceTaskId = $null

        foreach ($line in $sonarOutput) {
            if ($line -match $taskIdPattern) {
              $ceTaskId = $Matches[1]
              Write-Host "Matched line: $line"
              break
            }
        }

        if ($null -ne $ceTaskId) {
          Write-Host "Found ceTaskId: $ceTaskId"
          "ceTaskId=$ceTaskId" >> $env:GITHUB_OUTPUT
        } else {
          Write-Error "Could not extract ceTaskId from scanner output."
          exit 1
        }

    - name: Poll for SonarQube Task Completion
      shell: powershell
      run: |
        $ErrorActionPreference = "Stop"
        $ceTaskId = "${{ steps.end_analysis.outputs.ceTaskId }}"
        $apiUrl = "${{ inputs.sonarqube_url }}/api/ce/task?id=$ceTaskId"

        $authHeader = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${{ inputs.sonarqube_token }}:"))

        $maxRetries = 10
        $waitSeconds = 10

        for ($i = 0; $i -lt $maxRetries; $i++) {
          Start-Sleep -Seconds $waitSeconds
          try {
            $response = Invoke-RestMethod -Uri $apiUrl -Headers @{ "Authorization" = $authHeader }
            $status = $response.task.status
            Write-Host "Task status: $status"
            if ($status -eq "SUCCESS") {
              Write-Host "SonarQube background task completed."
              break
            } elseif ($status -eq "FAILED") {
              Write-Error "SonarQube background task failed."
              exit 1
            } else {
              Write-Host "Waiting... ($i/$maxRetries)"
            }
          } catch {
            Write-Warning "Error polling SonarQube task: $_"
          }
        }
