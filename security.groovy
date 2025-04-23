- name: Run SonarQube Full Analysis
  uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/dotnet-build-sonar-scan@main
  with:
    SonarQubeProjectName: ${{ inputs.SonarQubeProjectName }}
    sonarqube_url: ${{ inputs.sonarqube_url }}
    sonarqube_token: ${{ inputs.sonarqube_token }}
    SonarQubeExclusions: '**/bin/**,**/obj/**'
    SonarQubeCoverletReportPaths: ${{ inputs.SonarQubeCoverletReportPaths }}
    SolutionFile: ${{ inputs.SolutionFile }}
    PathToTestProject: ${{ inputs.PathToTestProject }}
    BuildArgs: ${{ inputs.BuildArgs }}
    SonarprojectBaseDir: ${{ inputs.SonarprojectBaseDir }}
