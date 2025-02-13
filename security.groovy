name: SonarQube Pre-Build

on:
  workflow_call:
    inputs:
      SonarQubeProjectName:
        required: true
        type: string
      SonarQubeExclusions:
        required: true
        type: string
      SonarQubeCoverageReportPaths:
        required: true
        type: string
      SonarQubeTextExecutionReportPaths:
        required: true
        type: string
      SonarQubeServiceConnection:
        required: false
        type: string
        default: 'GBT Code Analysis'
      SonarQubeSources:
        required: true
        type: string
      SonarQubeTestSources:
        required: true
        type: string
      SonarprojectBaseDir:
        required: true
        type: string
      PipelineFolder:
        required: true
        type: string  # Pass this dynamically in each team's workflow

jobs:
  sonarqube-pre-build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Find and Load Environment Variables
        run: |
          ENV_FILE=$(find . -type f -path "*/${{ inputs.PipelineFolder }}/.env" | head -n 1)
          if [ -f "$ENV_FILE" ]; then
            echo "Loading environment variables from $ENV_FILE"
            export $(grep -v '^#' "$ENV_FILE" | xargs)
          else
            echo "No .env file found in ${{ inputs.PipelineFolder }}, skipping"
          fi
        shell: bash

      - name: Prepare SonarQube for Scan
        uses: sonarsource/sonarqube-scan-action@master
        with:
          sonarHostUrl: ${{ secrets.SONAR_HOST_URL }}
          sonarToken: ${{ secrets.SONAR_TOKEN }}
          args: >
            -Dsonar.projectKey=${{ inputs.SonarQubeProjectName }}
            -Dsonar.projectBaseDir=${{ inputs.SonarprojectBaseDir }}
            -Dsonar.projectVersion=${{ env.Build_BuildNumber }}
            -Dsonar.sources=${{ inputs.SonarQubeSources }}
            -Dsonar.tests=${{ inputs.SonarQubeTestSources }}
            -Dsonar.exclusions=${{ inputs.SonarQubeExclusions }}
            -Dsonar.test.exclusions=${{ inputs.SonarQubeExclusions }}
            -Dsonar.coverage.exclusions=**/*Tests*.cs,**/Models/**/*,**/Data/**/*,**/Program.cs,**/Startup.cs,${{ inputs.SonarQubeExclusions }}
            -Dsonar.javascript.lcov.reportPaths=${{ inputs.SonarQubeCoverageReportPaths }}
            -Dsonar.testExecutionReportPaths=${{ inputs.SonarQubeTextExecutionReportPaths }}
            -Dsonar.test.inclusions=**/*.test.jsx, **/*.test.js, **/*.test.tsx, **/*.test.ts
