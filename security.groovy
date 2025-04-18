name: SonarQube Analyze and Publish
description: Run sonar-scanner and wait for quality gate result

inputs:
  projectName:
    required: true
    description: SonarQube project key
  sonarHostUrl:
    required: true
    description: SonarQube server URL
  sonarToken:
    required: true
    description: SonarQube authentication token
  additionalArgs:
    required: false
    default: ''
    description: Additional sonar-scanner args

runs:
  using: "composite"
  steps:
    - run: |
        powershell ./github/actions/sonarqube-analyze/run.ps1 `
          -ProjectName "${{ inputs.projectName }}" `
          -SonarHostUrl "${{ inputs.sonarHostUrl }}" `
          -SonarToken "${{ inputs.sonarToken }}" `
          -AdditionalArgs "${{ inputs.additionalArgs }}"
      shell: pwsh
