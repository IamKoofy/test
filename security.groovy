name: Build Nightly

on:
  schedule:
    - cron: "20 2 * * 1-5"  # Runs at 2:20 AM Monday to Friday
  workflow_dispatch:  # Allows manual trigger

env:
  MajorVersion: ${{ secrets.MAJOR_VERSION }}
  MinorVersion: ${{ secrets.MINOR_VERSION }}
  BuildNumber: ${{ github.run_number }}
  Rev: ${{ github.run_attempt }}
  SolutionFile: ${{ secrets.SOLUTION_FILE }}
  PathToMainProject: ${{ secrets.MAIN_PROJECT_PATH }}
  PathToTestProject: ${{ secrets.TEST_PROJECT_PATH }}
  DockerFile: ${{ secrets.DOCKER_FILE }}
  DockerRepo: ${{ secrets.DOCKER_DEV_REPO }}
  DockerImageName: ${{ secrets.DOCKER_IMAGE_NAME }}
  SonarQubeProjectName: ${{ secrets.SONAR_PROJECT_NAME }}
  SonarQubeExclusions: ${{ secrets.SONAR_EXCLUSION }}
  SonarQubeCoverletReportPaths: ${{ secrets.COVERLET_REPORT_PATHS }}
  DotnetBuildOutputDockerSrc: ${{ secrets.DOTNET_BUILD_OUTPUT_DOCKER_SRC }}
  SnykAuthToken: ${{ secrets.SNYK_AUTH_TOKEN }}
  SnykOrg: ${{ secrets.SNYK_ORG }}
  SnykProjectName: ${{ secrets.SNYK_PROJECT_NAME }}
  SnykScanEnabled: ${{ secrets.SNYK_SCAN_ENABLED }}

jobs:
  build:
    runs-on: self-hosted  # Use your GitHub self-hosted runner

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Checkout templates repository
        uses: actions/checkout@v4
        with:
          repository: DotNet-COP/devops-templates
          ref: Development
          token: ${{ secrets.GIT_TOKEN }}

      - name: Run Build Process
        uses: ./.github/actions/build/dotnet-core-docker-v2
        with:
          MajorVersion: ${{ env.MajorVersion }}
          MinorVersion: ${{ env.MinorVersion }}
          BuildNumber: ${{ env.BuildNumber }}
          Rev: ${{ env.Rev }}
          SolutionFile: ${{ env.SolutionFile }}
          PathToMainProject: ${{ env.PathToMainProject }}
          PathToTestProject: ${{ env.PathToTestProject }}
          DockerFile: ${{ env.DockerFile }}
          DockerRepo: ${{ env.DockerRepo }}
          DockerImageName: ${{ env.DockerImageName }}
          SonarQubeProjectName: ${{ env.SonarQubeProjectName }}
          SonarQubeExclusions: ${{ env.SonarQubeExclusions }}
          SonarQubeCoverletReportPaths: ${{ env.SonarQubeCoverletReportPaths }}
          DotnetBuildOutputDockerSrc: ${{ env.DotnetBuildOutputDockerSrc }}
          SnykAuthToken: ${{ env.SnykAuthToken }}
          SnykOrg: ${{ env.SnykOrg }}
          SnykProjectName: ${{ env.SnykProjectName }}
          SnykScanEnabled: ${{ env.SnykScanEnabled }}
