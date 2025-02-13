name: SonarQube Post-Build

on:
  workflow_call:
    inputs:
      SonarQubeProjectName:
        required: true
        type: string
      SonarprojectBaseDir:
        required: true
        type: string
    secrets:
      SONAR_HOST_URL:
        required: true
      SONAR_TOKEN:
        required: true

jobs:
  sonarqube-post-build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Remove sonar.branch.name Variable
        shell: pwsh
        run: |
          if ($env:SONARQUBE_SCANNER_PARAMS) {
            $sonarSettings = $env:SONARQUBE_SCANNER_PARAMS | ConvertFrom-Json
            $sonarSettings.PSObject.Properties.Remove("sonar.branch.name")
            $updatedSonarSettings = $sonarSettings | ConvertTo-Json -compress
            echo "##vso[task.setvariable variable=SONARQUBE_SCANNER_PARAMS]$updatedSonarSettings"
            Write-Host $updatedSonarSettings
          } else {
            Write-Host "No SONARQUBE_SCANNER_PARAMS found, skipping..."
          }

      - name: Run SonarQube Code Analysis
        uses: sonarsource/sonarqube-scan-action@master
        with:
          sonarHostUrl: ${{ secrets.SONAR_HOST_URL }}
          sonarToken: ${{ secrets.SONAR_TOKEN }}
          args: >
            -Dsonar.projectBaseDir=${{ inputs.SonarprojectBaseDir }}

      - name: Publish SonarQube Results
        run: |
          echo "SonarQube analysis completed."
          echo "Waiting for SonarQube results..."
          sleep 300  # Simulates polling for 300 seconds (adjust as needed)
        shell: bash
