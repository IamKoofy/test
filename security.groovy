name: Build

on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Select the deployment environment"
        required: true
        default: "Dev"
        type: choice
        options:
          - Dev
          - Cert

env:
  GIT_PAT: ${{ secrets.GIT_PAT }}
  SNYK_AUTH_TOKEN: ${{ secrets.SNYK_AUTH_TOKEN }}
  SNYK_ORG: ${{ secrets.SNYK_ORG }}
  SNYK_PROJECT_NAME: ${{ secrets.SNYK_PROJECT_NAME }}
  SonarQubeToken: ${{ secrets.SONARQUBE_TOKEN }}
  git_access_token: ${{ secrets.GIT_ACCESS_TOKEN }}
  docker_username: ${{ secrets.DOCKER_USERNAME }}
  docker_password: ${{ secrets.DOCKER_PASSWORD }}

jobs:
  build-app:
    runs-on: [self-hosted, windows]

    steps:
      - name: Checkout repo
        uses: actions/checkout@v4

      - name: Load Pipeline Variables
        id: load-vars
        shell: powershell
        run: |
          Write-Host "Loading pipeline variables from build.vars.yml"
          $yamlPath = "$env:GITHUB_WORKSPACE/.github/workflows/build.vars.yml"

          if (Test-Path $yamlPath) {
            $content = Get-Content $yamlPath | Where-Object {$_ -match '^\s*[^#]'}
            foreach ($line in $content) {
              $key, $value = $line -split ":\s*", 2
              if ($key -and $value) {
                echo "$key=$value" | Out-File -Append -Encoding utf8 $env:GITHUB_ENV
              }
            }
          } else {
            Write-Error "build.vars.yml not found!"
            exit 1
          }

      - name: Clone GitHub Actions Shared Lib
        uses: actions/checkout@v4
        with:
          repository: AMEX-GBTG-Sandbox/github-actions-shared-lib
          ref: main
          token: ${{ secrets.GIT_PAT }}
          path: github-actions-shared-lib

      - name: Call Composite Action
        uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/dotnet-core-docker-gitclone@main
        with:
          GithubRepo: ${{ env.GithubRepo }}
          Branch: ${{ env.Branch }}
          SonarprojectBaseDir: ${{ env.SonarprojectBaseDir }}
          MajorVersion: ${{ env.MajorVersion }}
          MinorVersion: ${{ env.MinorVersion }}
          BuildNumber: ${{ env.BuildNumber }}
          Rev: ${{ env.Rev }}
          SolutionFile: ${{ env.ProjectPath }}
          PathToMainProject: ${{ env.MainProjectPath }}
          PathToTestProject: ${{ env.TestProjectPath }}
          DockerFile: ${{ env.Dockerfile }}
          DockerRepo: ${{ env.DockerDevRepo }}
          DockerImageName: ${{ env.DockerImageName }}
          SonarQubeProjectName: ${{ env.SonarProjectName }}
          SonarQubeExclusions: ${{ env.SonarExclusion }}
          SonarQubeCoverletReportPaths: ${{ env.CoverletReportPaths }}
          DotnetBuildOutputDockerSrc: ${{ env.DotnetBuildOutputDockerSrc }}
          SnykAuthToken: ${{ secrets.SNYK_AUTH_TOKEN }}
          SnykOrg: ${{ secrets.SNYK_ORG }}
          SnykProjectName: ${{ secrets.SNYK_PROJECT_NAME }}
