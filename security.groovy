name: Adservice Build

on:
  workflow_dispatch:

env:
  GITHUB_PAT: ${{ secrets.GITHUB_PAT }}  # Using GitHub secret for authentication
  SNYK_AUTH_TOKEN: ${{ secrets.SNYK_AUTH_TOKEN }}
  SNYK_ORG: ${{ secrets.SNYK_ORG }}
  SNYK_PROJECT_NAME: ${{ secrets.SNYK_PROJECT_NAME }}

jobs:
  build-app:
    runs-on: self-hosted
    steps:
      - name: Clone GitHub Templates Repo
        shell: pwsh
        run: |
          Write-Host "Cloning Git templates"
          git clone https://github.com/TEST/Azure-Pipelines.git templates
          cd templates
          git checkout main

      - name: Clone GitHub Repo
        shell: pwsh
        run: |
          Write-Host "Cloning Git repo"
          git clone https://$env:GITHUB_PAT@github.com/TEST/MilkyWay-TravelConfirmation.git MilkyWayTravelConfirmation
          cd MilkyWayTravelConfirmation
          git checkout main

      - name: Run Base Workflow (hrg-dotnet-framework)
        uses: your-org/your-repo/.github/workflows/hrg-dotnet-framework.template.yml@main
        with:
          VersioningTaskGroupFilename: 'task-groups/version-build.taskgroup.yml'
          MajorVersion: ${{ vars.MajorVersion }}
          MinorVersion: ${{ vars.MinorVersion }}
          BuildNumber: ${{ vars.BuildNumber }}
          Rev: ${{ vars.Rev }}
          PathToProject: ${{ vars.PathToProject }}
          SolutionFile: ${{ vars.SolutionFile }}
          PathToTestProject: ${{ vars.PathToTestProject }}
          ComponentName: ${{ vars.ComponentName }}
          BuildConfiguration: ${{ vars.BuildConfiguration }}
          SonarQubeProjectName: ${{ vars.SonarQubeProjectName }}
          SonarQubeExclusions: ${{ vars.SonarQubeExclusions }}
          SnykAuthToken: ${{ secrets.SNYK_AUTH_TOKEN }}
          SnykOrg: ${{ secrets.SNYK_ORG }}
          SnykProjectName: ${{ secrets.SNYK_PROJECT_NAME }}
