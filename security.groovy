name: sbp-admin-web.build

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
      branch:
        description: "Branch to build (e.g. main, Release-107)"
        required: true
        default: "main"
        type: string

env:
  GIT_PAT: ${{secrets.GIT_PAT}}
  SNYK_AUTH_TOKEN: ${{secrets.SNYK_AUTH_TOKEN}}
  SNYK_ORG: ${{secrets.SNYK_ORG}}
  SNYK_PROJECT_NAME: ${{secrets.SNYK_PROJECT_NAME}}
  SonarQubeToken: ${{ secrets.SONARQUBE_TOKEN }}
  git_access_token: ${{ secrets.GIT_ACCESS_TOKEN }}
  docker_username: ${{ secrets.DOCKER_USERNAME }}
  docker_password: ${{ secrets.DOCKER_PASSWORD }}

jobs:
  build-app:
    runs-on: [self-hosted, windows, ADO1]

    steps:

      - name: checkout repo (target branch)
        uses: actions/checkout@v4
        with:
          ref: ${{ github.event.inputs.branch }}

      - name: Load Pipeline Variables
        id: load-vars
        shell: powershell
        run: |
          Write-Host "Loading pipeline variables from Build.vars.yml"
          $yamlPath = "$env:GITHUB_WORKSPACE\.github\workflows\sbp-admin-web.variables.yml"

          if (Test-Path $yamlPath) {
            $content = Get-Content $yamlPath | Where-Object {$_ -match '^\s*[^#]'}
            foreach ($line in $content) {
              $key, $value = $line -split ":\s*", 2
              if ($key -and $value) {
                echo "$key=$value" | Out-File -Append -Encoding utf8 $env:GITHUB_ENV
                echo "::set-output name=$key::$value"
              }
            }
          } else {
            Write-Error "Build.vars.yml not found!"
            exit 1
          }

      - name: debug
        shell: powershell
        run: |
          Get-ChildItem -Path Env:* | Sort-Object Name | Format-Table -AutoSize

      - name: Clone GitHub Repo
        uses: actions/checkout@v4
        with:
          repository: AMEX-GBTG-Sandbox/github-actions-shared-lib
          ref: main
          token: ${{ secrets.GIT_PAT }}
          path: github-actions-shared-lib

      - name: Login to Docker
        shell: powershell
        run: |
          $tag = "${{ env.DockerDevRepo }}/${{ env.DockerImageName }}:${{ github.run_number }}"
          echo "DOCKER_IMAGE_TAG=$tag" | Out-File -FilePath $env:GITHUB_ENV -Encoding utf8

      - name: call the other template
        uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/javascript-typescript-docker-v2-gitclone.template@main
        with:
          GithubRepo: ${{ env.GithubRepo }}
          Branch: ${{ github.event.inputs.branch }}
          ProjectDirectory: ${{ env.ProjectDirectory }}
          SonarprojectBaseDir: ${{ env.SonarprojectBaseDir }}
          LintingEnabled: false
          TypescriptCheckingEnabled: false
          YarnBuildArgs: ${{ env.YarnBuildArgs }}
          EnvFilePath: ${{ env.EnvFilePath }}
          EnvBuildNumberUpdateEnabled: true
          EnvBuildNumberFieldName: 'REACT_APP_ELT_VERSION'
          MajorVersion: ${{ env.MajorVersion }}
          MinorVersion: ${{ env.MinorVersion }}
          BuildNumber: ${{ env.BuildNumber }}
          Rev: ${{ env.Rev }}
          Dockerfile: ${{ env.Dockerfile }}
          DockerVersionArgs: ${{ env.Build.BuildNumber }}
          DockerRepo: ${{ env.DockerDevRepo }}
          DockerImageName: ${{ env.DockerImageName }}
          DockerFolder: ${{ env.DockerFolder }}
          SonarQubeProjectName: ${{ env.SonarProjectName }}
          SonarQubeExclusions: ${{ env.SonarExclusion }}
          SonarQubeCoverageReportPaths: ${{ env.SonarQubeCoverageReportPaths }}
          SonarQubeTextExecutionReportPaths: ${{ env.SonarQubeTextExecutionReportPaths }}
          SonarQubeSources: ${{ env.SonarQubeSources }}
          SonarQubeTestSources: ${{ env.SonarQubeTestSources }}
          SnykAuthToken: ${{ secrets.SNYK_AUTH_TOKEN }}
          SnykOrg: ${{ secrets.SNYK_ORG }}
          SnykProjectName: ${{ secrets.SNYK_PROJECT_NAME }}
          token: ${{ secrets.GIT_PAT }}
          git_access_token: ${{ secrets.GIT_ACCESS_TOKEN }}
          docker_username: ${{ secrets.DOCKER_USERNAME }}
          docker_password: ${{ secrets.DOCKER_PASSWORD }}
