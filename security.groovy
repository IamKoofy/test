- name: Run SonarQube Full Analysis
  shell: powershell
  run: ./sonar-wrap-build-test.ps1
  env:
    ProjectKey: ${{ inputs.SonarQubeProjectName }}
    SonarQubeUrl: ${{ inputs.sonarqube_url }}
    SonarToken: ${{ inputs.sonarqube_token }}
    Exclusions: '**/bin/**,**/obj/**'
    CoverageReport: ${{ inputs.SonarQubeCoverletReportPaths }}
    SolutionFile: ${{ inputs.SolutionFile }}
    TestProject: ${{ inputs.PathToTestProject }}
    BuildArgs: ${{ inputs.BuildArgs }}
