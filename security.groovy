name: 'Core API Build'
description: 'Builds and tests a .NET Core project, performs SonarQube analysis, Snyk scan, and builds a Docker image.'
inputs:
  MajorVersion:
    required: true
    type: string
  MinorVersion:
    required: true
    type: string
  GithubRepo:
    required: true
    type: string
  Branch:
    required: true
    type: string
  BuildNumber:
    required: true
    type: string
  Rev:
    required: true
    type: string
  SolutionFile:
    required: true
    type: string
  PathToMainProject:
    required: true
    type: string
  PathToTestProject:
    required: true
    type: string
  DotnetBuildOutputDockerSrc:
    required: true
    type: string
  BuildArgs:
    required: false
    type: string
    default: '-c Release'
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
  SonarQubeProjectName:
    required: true
    type: string
  SonarQubeExclusions:
    required: false
    type: string
  SonarQubeCoverletReportPaths:
    required: false
    type: string
  SonarQubeServiceConnection:
    required: false
    type: string
    default: 'GBT Code Analysis'
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
  SonarprojectBaseDir:
    required: false
    type: string

runs:
  using: "composite"
  steps:

    - name: Setup .NET 6.0
      uses: actions/setup-dotnet@v3
      with:
        dotnet-version: '6.0.x'

    - name: Clone GitHub Repo
      shell: pwsh
      run: |
        $gitUrl = "${{ inputs.GithubRepo }}"
        $branch = "${{ inputs.Branch }}"
        $targetDir = "D:\BuildAgents\1\_work\_temp\ELT"
        $finalDir = "${{ github.workspace }}"
        git clone -b $branch $gitUrl $finalDir
        Write-Host "Repo cloned to: $finalDir"
        Get-ChildItem $finalDir -Recurse | ForEach-Object { Write-Host $_.FullName }

    - name: Display Pipeline Info
      uses: ./actions/display-pipeline-info.taskgroup@main

    - name: Versioning
      uses: ./actions/version-build.taskgroup@main
      with:
        MajorVersion: ${{ inputs.MajorVersion }}
        MinorVersion: ${{ inputs.MinorVersion }}
        BuildNumber: ${{ inputs.BuildNumber }}
        Rev: ${{ inputs.Rev }}

    - name: SonarQube Pre-Build Analysis
      uses: ./actions/sonarqube-pre-build-dotnet-workdir.taskgroup@main
      with:
        SonarQubeProjectName: ${{ inputs.SonarQubeProjectName }}
        SonarQubeExclusions: ${{ inputs.SonarQubeExclusions }}
        SonarQubeCoverletReportPaths: ${{ inputs.SonarQubeCoverletReportPaths }}
        SonarQubeServiceConnection: ${{ inputs.SonarQubeServiceConnection }}
        SonarprojectBaseDir: ${{ inputs.SonarprojectBaseDir }}

    - name: Build .NET Core Project
      shell: pwsh
      run: dotnet build "${{ inputs.SolutionFile }}" ${{ inputs.BuildArgs }}

    - name: Snyk Security Scan
      if: inputs.SnykScanEnabled == 'true'
      uses: ./actions/snyk-dotnet-scan.taskgroup@main
      with:
        SnykAuthToken: ${{ inputs.SnykAuthToken }}
        SnykSolutionFile: ${{ inputs.SolutionFile }}
        SnykOrg: ${{ inputs.SnykOrg }}
        SnykProjectName: ${{ inputs.SnykProjectName }}

    - name: Run Unit and Component Tests
      shell: pwsh
      run: |
        dotnet test "${{ inputs.PathToTestProject }}" ${{ inputs.BuildArgs }} `
          --filter "(TestCategory=Unit)|(TestCategory=Component)" `
          --collect "Code Coverage" `
          /p:EnableNETAnalyzers=false

    - name: SonarQube Post-Build Analysis
      uses: ./actions/sonarqube-post-build-workdir.taskgroup@main
      with:
        SonarQubeProjectName: ${{ inputs.SonarQubeProjectName }}
        SonarprojectBaseDir: ${{ inputs.SonarprojectBaseDir }}

    - name: Publish .NET Core Project
      shell: pwsh
      run: |
        dotnet publish "${{ inputs.PathToMainProject }}" `
          --no-build --no-restore `
          -o "d:\DockerShare\${{ runner.id }}\${{ github.run_id }}${{ inputs.DotnetBuildOutputDockerSrc }}" `
          ${{ inputs.BuildArgs }}

    - name: Build Docker Image
      uses: ./actions/docker-build.taskgroup@main
      with:
        Dockerfile: ${{ inputs.Dockerfile }}
        DockerRepo: ${{ inputs.DockerRepo }}
        DockerImageName: ${{ inputs.DockerImageName }}
        DockerVersionArgs: ${{ inputs.DockerVersionArgs }}
        DockerContext: 'docker-unix'
        DockerFolder: "d:\DockerShare\${{ runner.id }}\${{ github.run_id }}"

    - name: Clean Up Docker Share
      shell: pwsh
      continue-on-error: true
      run: Remove-Item -Path "d:\DockerShare\${{ runner.id }}\${{ github.run_id }}" -Recurse -Force

    - name: Push Docker Image
      uses: ./actions/docker-push.taskgroup@main
      with:
        DockerRepo: ${{ inputs.DockerRepo }}
        DockerImageName: ${{ inputs.DockerImageName }}
        DockerVersionArgs: ${{ inputs.DockerVersionArgs }}
