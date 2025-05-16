parameters:
  - name: SonarQubeProjectName
    type: string
  - name: SonarQubeExclusions
    type: string    
  - name: SonarQubeCoverletReportPaths
    type: string
  - name: SonarQubeServiceConnection
    type: string
    default: 'GBT Code Analysis'
  - name: gitFolder
    type: string

steps:
- task: PowerShell@2
  displayName: 'prep proj files for sonarqube scan'
  inputs:
    targetType: 'inline'
    script: |
      function update-project-file($file)
      {
          $fileContent = Get-Content -path $file.FullName
          if ($fileContent -like "*<ProjectGuid>*")
          {
              Write-Host "$($file.Name) already has a project id assigned"
              return $false
          }

          [xml]$xmlFile = $fileContent
          $node = $xmlFile.SelectSingleNode("//Project/PropertyGroup")
          if (-not $node) {
              Write-Warning "$($file.Name): No <PropertyGroup> found"
              return $false
          }

          $child = $xmlFile.CreateElement("ProjectGuid")
          $child.InnerText = "{" + [guid]::NewGuid().ToString().ToUpper() + "}"
          $node.AppendChild($child)
          $xmlFile.Save($file.FullName) | Out-Null
          Write-Host "$($file.Name) has been assigned a new project id"
          return $true
      }

      $projectRoot = "$(Build.SourcesDirectory)\${{ parameters.gitFolder }}"
      Write-Host "Scanning project root: $projectRoot"

      $csprojFiles = Get-ChildItem -Path $projectRoot -Filter *.csproj -Recurse -File
      foreach ($file in $csprojFiles) {
          if ($file -ne $null) {
              update-project-file $file | Out-Null
          }
      }

- task: SonarQubePrepare@4
  displayName: 'prep sonarqube for scan'
  continueOnError: true
  inputs:
    SonarQube: '${{ parameters.SonarQubeServiceConnection }}'
    scannerMode: 'MSBuild'
    projectKey: '${{ parameters.SonarQubeProjectName }}'
    projectVersion: '$(Build.BuildNumber)'
    extraProperties: |
      sonar.exclusions=${{ parameters.SonarQubeExclusions }}
      sonar.test.exclusions=${{ parameters.SonarQubeExclusions }}
      sonar.coverage.exclusions=**/*Tests*.cs,**/Models/**/*,**/Data/**/*,**/Program.cs,**/Startup.cs,${{ parameters.SonarQubeExclusions }}
      sonar.cs.roslyn.ignoreIssues=false
##      sonar.cs.opencover.reportsPaths=${{ parameters.SonarQubeCoverletReportPaths }}
