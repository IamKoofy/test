name: 'DotNet Core Docker V2'
description: 'Composite GitHub Action for .NET Core build and Docker pipeline'
inputs:
  MajorVersion:
    required: true
  MinorVersion:
    required: true
  BuildNumber:
    required: true
  Rev:
    required: true
  SolutionFile:
    required: true
  PathToMainProject:
    required: true
  PathToTestProject:
    required: true
  DotnetBuildOutputDockerSrc:
    required: true
  BuildArgs:
    required: false
    default: '-c Release'
  Dockerfile:
    required: true
  DockerVersionArgs:
    required: false
    default: '${{ github.run_number }}'
  DockerRepo:
    required: true
  DockerImageName:
    required: true
  SonarQubeProjectName:
    required: true
  SonarQubeExclusions:
    required: true
  SonarQubeCoverletReportPaths:
    required: true
  SonarQubeServiceConnection:
    required: false
    default: 'GBT Code Analysis'
  SnykScanEnabled:
    required: false
    default: 'true'
  SnykAuthToken:
    required: true
  SnykOrg:
    required: true
  SnykProjectName:
    required: true

runs:
  using: "composite"
  steps:

    - uses: DotNet-COP/display-pipeline-info@main

    - uses: DotNet-COP/version-build@main
      with:
        MajorVersion: ${{ inputs.MajorVersion }}
        MinorVersion: ${{ inputs.MinorVersion }}
        BuildNumber: ${{ inputs.BuildNumber }}
        Rev: ${{ inputs.Rev }}

    - uses: DotNet-COP/sonarqube-pre-build-dotnet@main
      with:
        SonarQubeProjectName: ${{ inputs.SonarQubeProjectName }}
        SonarQubeExclusions: ${{ inputs.SonarQubeExclusions }}
        SonarQubeCoverletReportPaths: ${{ inputs.SonarQubeCoverletReportPaths }}
        SonarQubeServiceConnection: ${{ inputs.SonarQubeServiceConnection }}

    - name: Build Core Project
      shell: pwsh
      run: |
        dotnet build "${{ inputs.SolutionFile }}" ${{ inputs.BuildArgs }}

    - uses: DotNet-COP/snyk-dotnet-scan@main
      with:
        SnykScanEnabled: ${{ inputs.SnykScanEnabled }}
        SnykAuthToken: ${{ inputs.SnykAuthToken }}
        SnykSolutionFile: ${{ inputs.SolutionFile }}
        SnykOrg: ${{ inputs.SnykOrg }}
        SnykProjectName: ${{ inputs.SnykProjectName }}

    - name: Run Tests
      shell: pwsh
      run: |
        dotnet test "${{ inputs.PathToTestProject }}" ${{ inputs.BuildArgs }} --filter "(TestCategory=Unit)|(TestCategory=Component)" --collect "Code Coverage" /p:EnableNETAnalyzers=false

    - uses: DotNet-COP/sonarqube-post-build@main
      with:
        SonarQubeProjectName: ${{ inputs.SonarQubeProjectName }}

    - name: Publish Core Project
      shell: pwsh
      run: |
        dotnet publish "${{ inputs.PathToMainProject }}" --no-build --no-restore -o d:/DockerShare/${{ github.run_id }}${{ inputs.DotnetBuildOutputDockerSrc }} ${{ inputs.BuildArgs }}

    - uses: DotNet-COP/docker-build@main
      with:
        Dockerfile: ${{ inputs.Dockerfile }}
        DockerRepo: ${{ inputs.DockerRepo }}
        DockerImageName: ${{ inputs.DockerImageName }}
        DockerVersionArgs: ${{ inputs.DockerVersionArgs }}
        DockerContext: 'docker-unix'
        DockerFolder: 'd:/DockerShare/${{ github.run_id }}'

    - name: Clean up Docker Share
      shell: pwsh
      continue-on-error: true
      run: |
        Remove-Item -Recurse -Force "d:/DockerShare/${{ github.run_id }}"

    - uses: DotNet-COP/docker-push@main
      with:
        DockerRepo: ${{ inputs.DockerRepo }}
        DockerImageName: ${{ inputs.DockerImageName }}
        DockerVersionArgs: ${{ inputs.DockerVersionArgs }}
