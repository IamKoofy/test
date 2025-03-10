name: Build and Scan

on:
  workflow_dispatch:

env:
  GIT_PAT: ${{ secrets.GIT_PAT }}
  SNYK_AUTH_TOKEN: ${{ secrets.SNYK_AUTH_TOKEN }}
  SNYK_ORG: ${{ secrets.SNYK_ORG }}
  SNYK_PROJECT_NAME: ${{ secrets.SNYK_PROJECT_NAME }}
  SONARQUBE_TOKEN: ${{ secrets.SONARQUBE_TOKEN }}
  GIT_ACCESS_TOKEN: ${{ secrets.GIT_ACCESS_TOKEN }}

jobs:
  build-app:
    runs-on: [self-hosted, windows]
    steps:
      - name: Checkout Repo
        uses: actions/checkout@v4

      - name: Load Pipeline Variables
        id: load-vars
        shell: powershell
        run: |
          Write-Host "Loading pipeline variables from Build.vars.yml"
          $yamlPath = "$env:GITHUB_WORKSPACE/.github/workflows/Build.vars.yml"
          if (Test-Path $yamlPath) {
            $content = Get-Content $yamlPath | Where-Object {$_ -match '^\s*[^#]'} # Ignore comments
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

      - name: Debug
        shell: powershell
        run: Get-ChildItem -Path Env:* | Sort-Object Name | Format-Table -AutoSize

      - name: Clone GitHub Templates Repo
        shell: powershell
        run: |
          Write-Host "Cloning Git templates"
          $tempDir = New-Item -ItemType Directory -Path "$env:RUNNER_TEMP/templates"
          git clone https://$env:GIT_PAT@github.com/gbtg-devops/Azure-Pipelines.git $tempDir
          cd $tempDir
          git checkout main

      - name: Clone GitHub Repo
        shell: powershell
        run: |
          Write-Host "Cloning Git repo"
          $tempDir = New-Item -ItemType Directory -Path "$env:RUNNER_TEMP/MilkyWayTravelConfirmation"
          git clone https://$env:GIT_PAT@github.com/AEGBT/MilkyWay-TravelConfirmation.git $tempDir
          cd $tempDir
          git checkout main

      - name: Clone Test Templates
        shell: powershell
        run: |
          Write-Host "Cloning test templates"
          $tempDir = New-Item -ItemType Directory -Path "$env:RUNNER_TEMP/test-templates"
          git clone https://$env:GIT_PAT@github.com/AMEX-GBTG-Sandbox/test-templates.git $tempDir

      - name: Clone Shared Library
        shell: powershell
        run: |
          Write-Host "Cloning shared library"
          $tempDir = New-Item -ItemType Directory -Path "$env:RUNNER_TEMP/github-actions-shared-lib"
          git clone https://$env:GIT_PAT@github.com/AMEX-GBTG-Sandbox/github-actions-shared-lib $tempDir

      - name: Debug Workspace
        shell: powershell
        run: |
          Write-Host "Current Directory: $PWD"
          Get-ChildItem -Path $env:RUNNER_TEMP -Recurse

      - name: Run Build and Scanning Steps
        uses: AMEX-GBTG-Sandbox/test-templates/.github/actions/hrg-dotnet-framework.template@main
        with:
          GithubRepo: ${{ env.GithubRepo }}
          Branch: ${{ env.Branch }}
          ProjectDirectory: ${{ env.ProjectDirectory }}
          SonarprojectBaseDir: ${{ env.SonarprojectBaseDir }}
          LintingEnabled: false
          TypescriptCheckingEnabled: false
          YarnBuildArgs: 'build'
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
