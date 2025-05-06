name: 'Core Dotnet Build'
description: 'Builds and publishes a .NET Core/5/6/8 app, runs tests, performs SonarQube & Snyk analysis.'

inputs:
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

  ProjectName:
    required: true
    type: string
  PathToProject:
    required: true
    type: string
  PathToTestProject:
    required: true
    type: string
  SolutionFile:
    required: true
    type: string
  BuildArgs:
    required: false
    type: string
    default: '-c Release'
  NpmAuthenticate:
    required: false
    type: boolean
    default: false

  SonarQubeProjectName:
    required: true
    type: string
  SonarQubeExclusions:
    required: false
    type: string
  SonarQubeCoverletReportPaths:
    required: false
    type: string
  sonarqube_url:
    required: false
    type: string
  sonarqube_token:
    required: false
    type: string
  SonarprojectBaseDir:
    required: false
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
  git_access_token:
    required: true
    type: string

runs:
  using: "composite"
  steps:

    - name: Display Pipeline Info
      uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/display-pipeline-info.taskgroup@main

    - name: Versioning
      uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/version-use-buildnumber.taskgroup@main
      with:
        MajorVersion: ${{ inputs.MajorVersion }}
        MinorVersion: ${{ inputs.MinorVersion }}
        BuildNumber: ${{ inputs.BuildNumber }}
        Rev: ${{ inputs.Rev }}

    - name: Setup .NET 8
      uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/setup-dotnet@main
      with:
        dotnet-version: '8.0.x'

    - name: Check .NET SDK Version
      shell: powershell
      run: dotnet --info

    - name: Authenticate NPM and Install Dependencies
      if: ${{ inputs.NpmAuthenticate == 'true' }}
      shell: powershell
      run: |
        echo "//registry.npmjs.org/:_authToken=${{ secrets.NPM_TOKEN }}" > ${{ github.workspace }}\.npmrc
        npm ci --prefix ${{ github.workspace }}

    - name: Restore NuGet Packages
      shell: powershell
      run: dotnet restore ${{ inputs.SolutionFile }}

    - name: Build Core Project
      shell: powershell
      run: dotnet build ${{ inputs.PathToProject }} ${{ inputs.BuildArgs }}

    - name: Run SonarQube Full Analysis
      uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/dotnet-sonar@main
      with:
        SonarQubeProjectName: ${{ inputs.SonarQubeProjectName }}
        sonarqube_url: ${{ inputs.sonarqube_url }}
        sonarqube_token: ${{ inputs.sonarqube_token }}
        SonarQubeExclusions: ${{ inputs.SonarQubeExclusions }}
        SonarQubeCoverletReportPaths: ${{ inputs.SonarQubeCoverletReportPaths }}
        SolutionFile: ${{ inputs.SolutionFile }}
        PathToTestProject: ${{ inputs.PathToTestProject }}
        BuildArgs: ${{ inputs.BuildArgs }}
        SonarprojectBaseDir: ${{ inputs.SonarprojectBaseDir }}

    - name: Snyk Scan
      if: ${{ inputs.SnykScanEnabled == 'true' }}
      uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/snyk_dotnet_scan_taskgroup@main
      with:
        SnykAuthToken: ${{ inputs.SnykAuthToken }}
        SnykSolutionFile: ${{ inputs.SolutionFile }}
        SnykOrg: ${{ inputs.SnykOrg }}
        SnykProjectName: ${{ inputs.SnykProjectName }}
        git_access_token: ${{ inputs.git_access_token }}
      continue-on-error: true

    - name: Run Unit & Component Tests
      shell: powershell
      run: |
        dotnet test ${{ inputs.PathToTestProject }} ${{ inputs.BuildArgs }} --filter "(TestCategory=Unit)|(TestCategory=Component)" /p:CollectCoverage=true /p:CoverletOutputFormat=opencover

    - name: Publish .NET Project
      shell: powershell
      run: |
        dotnet publish ${{ inputs.PathToProject }} ${{ inputs.BuildArgs }} --no-restore -o ${{ github.workspace }}\publish /p:Version=${{ inputs.BuildNumber }}

    - name: Upload Published Artifact
      uses: actions/upload-artifact@v4
      with:
        name: ${{ inputs.ProjectName }}
        path: ${{ github.workspace }}\publish
