trigger: none
name: $(rev:r)

variables:
  - group: GihubPAT
  - template: travel-toolbox-ui-build.vars.yml

pool: main-build-pool

jobs:
  - job: BuildAllEnvirons
    steps:
      - powershell: |
          Write-Host "Cloning Git templates..."
          git clone https://github.com/TEST/Azure-Pipelines.git templates
          cd templates
          git checkout main

      - powershell: |
          Write-Host "Cloning Git repo..."
          $env:GITHUB_PAT = "$(GithubPAT)"
          git clone https://$env:GITHUB_PAT@github.com/TEST/gbc-travel-hero.git gbc-travel-hero
          cd gbc-travel-hero
          git checkout test-azure

      - task: UseNode@1
        inputs:
          version: '18.x'

      - task: DotNetCoreCLI@2
        inputs:
          command: custom
          custom: tool
          arguments: update --tool-path $(Build.SourcesDirectory)/gbc-travel-hero nbgv
        displayName: Install NBGV tool

      - script: .\nbgv cloud
        workingDirectory: '$(Build.SourcesDirectory)/gbc-travel-hero'
        displayName: Set Version

      - task: PowerShell@2
        displayName: Update .env with calculated version
        inputs:
          targetType: 'inline'
          script: |
            $version = [regex]::Escape("$(Build.BuildNumber)")
            $path = "$(Build.SourcesDirectory)/gbc-travel-hero"

            $envFiles = Get-ChildItem -Path $path -Filter ".env*"
            foreach($file in $envFiles) {
              $content = Get-Content -Path $file.FullName
              if($content -match 'VITE_APP_VERSION=') {
                $content = $content -replace '(VITE_APP_VERSION=).*', "VITE_APP_VERSION=$version"
              } else {
                $content += "`r`nVITE_APP_VERSION=$version"
              }
              Set-Content -Path $file.FullName -Value $content
            }

      - task: PowerShell@2
        displayName: Install Node Dependencies
        inputs:
          workingDirectory: '$(Build.SourcesDirectory)/gbc-travel-hero'
          targetType: inline
          script: |
            Write-Host "Current Directory: $(Get-Location)"
            if (Test-Path "package-lock.json") {
              Write-Host "Using npm ci"
              npm ci
            } else {
              Write-Host "Using npm install (no package-lock.json found)"
              npm install
            }
            if (Test-Path "node_modules") {
              Write-Host "✅ node_modules installed successfully."
            } else {
              Write-Error "❌ node_modules directory missing after install!"
              exit 1
            }

      - template: ../templates/build/task-groups/snyk-yarn-scan.taskgroup.yml
        parameters:
          SnykScanEnabled: ${{variables.SnykScanEnabled}}
          SnykAuthToken: ${{variables.SnykAuthToken}}
          SnykOrg: ${{variables.SnykOrg}}
          SnykProjectName: ${{variables.SnykProjectName}}
          SnykWorkingDirectory: '$(Build.SourcesDirectory)/gbc-travel-hero'

      - template: ../templates/build/task-groups/sonarqube-pre-build-typescript.taskgroup.yml
        parameters:
          SonarQubeProjectName: ${{ variables.SonarQubeProjectName }}
          SonarQubeExclusions: ${{ variables.SonarQubeExclusions }}
          SonarQubeTextExecutionReportPaths: ${{ variables.SonarQubeTextExecutionReportPaths }}
          SonarQubeSources: ${{ variables.SonarQubeSources }}
          SonarQubeTestSources: ${{ variables.SonarQubeTestSources }}
          SonarQubeCoverageReportPaths: ${{ variables.SonarQubeCoverageReportPaths }}

      - task: PowerShell@2
        displayName: Build UAT
        inputs:
          targetType: 'inline'
          workingDirectory: '$(Build.SourcesDirectory)/gbc-travel-hero'
          script: |
            npm i vite
            npm run build:uat -- --outDir dist-uat
            Compress-Archive -Path ./dist-uat/* -DestinationPath ./uat.zip -Force

      - task: PowerShell@2
        displayName: Build Production
        inputs:
          targetType: 'inline'
          workingDirectory: '$(Build.SourcesDirectory)/gbc-travel-hero'
          script: |
            npm run build -- --outDir dist-pro
            Compress-Archive -Path ./dist-pro/* -DestinationPath ./production.zip -Force

      - template: ../templates/build/task-groups/sonarqube-post-build.taskgroup.yml
        parameters:
          SonarQubeProjectName: ${{ variables.SonarQubeProjectName }}

      - task: PublishBuildArtifacts@1
        displayName: Publish UAT Artifact
        inputs:
          pathtoPublish: '$(Build.SourcesDirectory)/gbc-travel-hero/uat.zip'
          artifactName: UATArtifact

      - task: PublishBuildArtifacts@1
        displayName: Publish Production Artifact
        inputs: 
          pathtoPublish: '$(Build.SourcesDirectory)/gbc-travel-hero/production.zip' 
          artifactName: PROArtifact
