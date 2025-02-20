name: templates_build_task_groups_sonarqube_post_buildtaskgroup

inputs:
  SonarQubeProjectName:
    required: false
    type: string

runs:
  using: composite
  steps:
    - name: Remove sonar.branch.name Variable
      continue-on-error: true
      shell: powershell
      run: |
        $sonarSettings= $Env:SONARQUBE_SCANNER_PARAMS | ConvertFrom-Json
        $sonarSettings.PSObject.Properties.Remove("sonar.branch.name")
        $updatedSonarSettings = $sonarSettings | ConvertTo-Json -compress
        Write-Host "##vso[task.setvariable variable=SONARQUBE_SCANNER_PARAMS]$updatedSonarSettings"
        Write-Host $updatedSonarSettings

    - name: Run SonarQube Analysis
      continue-on-error: true
      shell: bash
      run: |
        echo "Running SonarQube Analysis..."
        sonar-scanner \
          -Dsonar.projectKey=${{ inputs.SonarQubeProjectName }} \
          -Dsonar.sources=. \
          -Dsonar.host.url=$SONARQUBE_URL \
          -Dsonar.login=$SONARQUBE_TOKEN

    - name: Publish SonarQube Result
      continue-on-error: true
      shell: bash
      run: |
        echo "Publishing SonarQube Result..."
        curl -u $SONARQUBE_TOKEN: "$SONARQUBE_URL/api/qualitygates/project_status?projectKey=${{ inputs.SonarQubeProjectName }}"
