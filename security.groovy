name: "Build AdService"

on:
  workflow_dispatch:

env:
  GITHUB_PAT: ${{ secrets.GITHUB_PAT }}
  SNYK_AUTH_TOKEN: ${{ secrets.SNYK_AUTH_TOKEN }}
  SNYK_ORG: ${{ secrets.SNYK_ORG }}
  SNYK_PROJECT_NAME: ${{ secrets.SNYK_PROJECT_NAME }}
  MAJOR_VERSION: ${{ vars.MajorVersion }}
  MINOR_VERSION: ${{ vars.MinorVersion }}
  BUILD_NUMBER: ${{ vars.BuildNumber }}
  REV: ${{ vars.Rev }}
  PATH_TO_PROJECT: ${{ vars.PathToProject }}
  SOLUTION_FILE: ${{ vars.SolutionFile }}
  PATH_TO_TEST_PROJECT: ${{ vars.PathToTestProject }}
  COMPONENT_NAME: ${{ vars.ComponentName }}
  BUILD_CONFIGURATION: ${{ vars.BuildConfiguration }}
  SONARQUBE_PROJECT_NAME: ${{ vars.SonarQubeProjectName }}
  SONARQUBE_EXCLUSIONS: ${{ vars.SonarQubeExclusions }}

jobs:
  build:
    name: "Building AdService"
    runs-on: main-build-pool
    steps:
      - name: "Checkout Azure Pipelines Templates"
        run: |
          echo "Cloning Git templates"
          git clone https://github.com/TEST/Azure-Pipelines.git templates
          cd templates
          git checkout main

      - name: "Checkout MilkyWay-TravelConfirmation Repo"
        run: |
          echo "Cloning Git repo"
          git clone https://${{ env.GITHUB_PAT }}@github.com/TEST/MilkyWay-TravelConfirmation.git MilkyWayTravelConfirmation
          cd MilkyWayTravelConfirmation
          git checkout main

      - name: "Build .NET Framework Application"
        uses: ./.github/actions/build-dotnet-framework
        with:
          VersioningTaskGroupFilename: 'task-groups/version-build.taskgroup.yml'
          MajorVersion: ${{ env.MAJOR_VERSION }}
          MinorVersion: ${{ env.MINOR_VERSION }}
          BuildNumber: ${{ env.BUILD_NUMBER }}
          Rev: ${{ env.REV }}
          PathToProject: ${{ env.PATH_TO_PROJECT }}
          SolutionFile: ${{ env.SOLUTION_FILE }}
          PathToTestProject: ${{ env.PATH_TO_TEST_PROJECT }}
          ComponentName: ${{ env.COMPONENT_NAME }}
          BuildConfiguration: ${{ env.BUILD_CONFIGURATION }}
          SonarQubeProjectName: ${{ env.SONARQUBE_PROJECT_NAME }}
          SonarQubeExclusions: ${{ env.SONARQUBE_EXCLUSIONS }}
          SnykAuthToken: ${{ env.SNYK_AUTH_TOKEN }}
          SnykOrg: ${{ env.SNYK_ORG }}
          SnykProjectName: ${{ env.SNYK_PROJECT_NAME }}
