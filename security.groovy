name: "Build Application"
description: "Composite action to build and push the application"

inputs:
  ProjectDirectory:
    required: true
    type: string
  GithubRepo:
    required: true
    type: string
  Branch:
    required: true
    type: string
  LintingEnabled:
    required: false
    type: boolean
    default: true
  TypescriptCheckingEnabled:
    required: false
    type: boolean
    default: true
  YarnBuildArgs:
    required: false
    type: string
  EnvFilePath:
    required: false
    type: string
  EnvBuildNumberUpdateEnabled:
    required: false
    type: boolean
    default: false
  EnvBuildNumberFieldName:
    required: false
    type: string
    default: ''
  EnvReleaseDateFieldName:
    required: false
    type: string
    default: 'RELEASE_DATE'
  MajorVersion:
    required: true
    type: string
  MinorVersion:
    required: true
    type: string
  BuildNumber:
    required: true
    type: string
  Rev:
    required: true
    type: string
  Dockerfile:
    required: true
    type: string
  DockerVersionArgs:
    required: false
    type: string
    default: '${{ github.run_number }}'
  DockerRepo:
    required: true
    type: string
  DockerImageName:
    required: true
    type: string
  DockerFolder:
    required: true
    type: string
  SnykScanEnabled:
    required: false
    type: boolean
    default: true
  SnykAuthToken:
    required: false
    type: string
  SnykOrg:
    required: false
    type: string
  SnykProjectName:
    required: false
    type: string
  SonarQubeProjectName:
    required: false
    type: string
  SonarQubeExclusions:
    required: false
    type: string
  SonarQubeCoverageReportPaths:
    required: false
    type: string
  SonarQubeTextExecutionReportPaths:
    required: false
    type: string
  SonarQubeServiceConnection:
    required: false
    type: string
    default: 'GBT Code Analysis'
  SonarQubeSources:
    required: false
    type: string
  SonarQubeTestSources:
    required: false
    type: string
  SonarprojectBaseDir:
    required: false
    type: string

runs:
  using: "composite"
  steps:
    - name: Clone GitHub Repo
      shell: pwsh
      run: |
        git clone -b ${{ inputs.Branch }} ${{ inputs.GithubRepo }}
        Write-Host "Repo cloned successfully"

    - name: Version the Application
      shell: pwsh
      run: |
        $buildNumber="${{ inputs.BuildNumber }}"
        $rev="${{ inputs.Rev }}"
        if ($buildNumber.Trim().ToLower() -eq 'auto') {
          $year = get-date –format yy
          $dayNumber=(Get-Date).DayOfYear.ToString().PadLeft(3,'0')
          $buildNumber="$year$dayNumber"
        }
        if ($rev.Trim().ToLower() -eq 'auto') {
          $split = $(${GITHUB_RUN_NUMBER}).Split('.')
          $rev = $split[$split.Count - 1]
        }
        $newBuildNumber = "${{inputs.MajorVersion}}.${{inputs.MinorVersion}}.$buildNumber.$rev"
        Write-Host "New Build Number: $newBuildNumber"

    - name: Restore Packages
      shell: pwsh
      run: |
        cd ${{ inputs.ProjectDirectory }}
        yarn install

    - name: Lint Code
      if: inputs.LintingEnabled == 'true'
      shell: pwsh
      run: |
        cd ${{ inputs.ProjectDirectory }}
        yarn run lint:cache

    - name: Run TypeScript Checks
      if: inputs.TypescriptCheckingEnabled == 'true'
      shell: pwsh
      run: |
        cd ${{ inputs.ProjectDirectory }}
        yarn run ts

    - name: Run Tests
      shell: pwsh
      run: |
        cd ${{ inputs.ProjectDirectory }}
        yarn run test:coverage

    - name: Yarn Build
      shell: pwsh
      run: |
        cd ${{ inputs.ProjectDirectory }}
        yarn run ${{ inputs.YarnBuildArgs }}

    - name: Docker Build
      uses: ./task-groups/docker-build
      with:
        Dockerfile: ${{ inputs.Dockerfile }}
        DockerRepo: ${{ inputs.DockerRepo }}
        DockerImageName: ${{ inputs.DockerImageName }}
        DockerVersionArgs: ${{ inputs.DockerVersionArgs }}
        DockerFolder: ${{ inputs.DockerFolder }}

    - name: Docker Push
      uses: ./task-groups/docker-push
      with:
        DockerRepo: ${{ inputs.DockerRepo }}
        DockerImageName: ${{ inputs.DockerImageName }}
        DockerVersionArgs: ${{ inputs.DockerVersionArgs }}
