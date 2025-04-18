name: 'SonarQube Post Analysis'
description: 'Runs dotnet sonarscanner end and checks SonarQube quality gate'
inputs:
  projectName:
    required: true
    description: 'SonarQube project key'
  sonarToken:
    required: true
    description: 'SonarQube token'
  sonarHostUrl:
    required: true
    description: 'SonarQube server URL'
  scannerParams:
    required: false
    description: 'SONARQUBE_SCANNER_PARAMS as JSON'

runs:
  using: "composite"
  steps:
    - name: Strip sonar.branch.name from SONARQUBE_SCANNER_PARAMS
      shell: pwsh
      run: |
        if ($env:SONARQUBE_SCANNER_PARAMS) {
          $settings = $env:SONARQUBE_SCANNER_PARAMS | ConvertFrom-Json
          $settings.PSObject.Properties.Remove("sonar.branch.name")
          $updated = $settings | ConvertTo-Json -Compress
          echo "Updated scanner params: $updated"
          echo "SONARQUBE_SCANNER_PARAMS=$updated" | Out-File -FilePath $env:GITHUB_ENV -Encoding utf8 -Append
        }

    - name: Install .NET SonarScanner tool
      shell: pwsh
      run: |
        dotnet tool install --global dotnet-sonarscanner
        echo "$HOME\.dotnet\tools" | Out-File -FilePath $env:GITHUB_PATH -Append -Encoding utf8

    - name: Run sonar end and publish quality gate status
      shell: pwsh
      run: |
        . "${{ github.action_path }}/sonar-post.ps1" `
          -ProjectName "${{ inputs.projectName }}" `
          -SonarToken "${{ inputs.sonarToken }}" `
          -SonarHostUrl "${{ inputs.sonarHostUrl }}"
