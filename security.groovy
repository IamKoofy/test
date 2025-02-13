# Reusable GitHub Actions workflow for .NET Framework Build & WebDeploy

name: HRG DotNet Framework Build

on:
  workflow_call:
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
      SolutionFile:
        required: true
        type: string
      PathToProject:
        required: true
        type: string
      PathToTestProject:
        required: true
        type: string
      ComponentName:
        required: true
        type: string
      BuildConfiguration:
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
      SonarQubeServiceConnection:
        required: false
        type: string
        default: 'GBT Code Analysis'
      VsTestEnabled:
        required: false
        type: boolean
        default: false

jobs:
  build:
    runs-on: windows-latest

    steps:
      - name: Checkout Source
        uses: actions/checkout@v4

      - name: Display Pipeline Info
        uses: ./templates/.github/workflows/display-pipeline-info.yml

      - name: Versioning Task Group
        uses: ./templates/.github/workflows/version-use-buildnumber.yml
        with:
          MajorVersion: ${{ inputs.MajorVersion }}
          MinorVersion: ${{ inputs.MinorVersion }}
          BuildNumber: ${{ inputs.BuildNumber }}
          Rev: ${{ inputs.Rev }}

      - name: SonarQube Pre-Build
        uses: ./templates/.github/workflows/sonarqube-pre-build.yml
        with:
          SonarQubeProjectName: ${{ inputs.SonarQubeProjectName }}
          SonarQubeExclusions: ${{ inputs.SonarQubeExclusions }}
          SonarQubeServiceConnection: ${{ inputs.SonarQubeServiceConnection }}

      - name: Authenticate with NuGet
        run: nuget sources add -Name "HRGTec MilkyWay Nuget" -Source "https://repos.gbt.gbtad.com/repository/nuget-api-v3/index.json"

      - name: Restore NuGet Packages
        run: nuget restore ${{ inputs.SolutionFile }}

      - name: Build Project
        run: msbuild ${{ inputs.SolutionFile }} /p:Configuration=${{ inputs.BuildConfiguration }}

      - name: Snyk Security Scan
        if: ${{ inputs.SnykScanEnabled }}
        uses: ./templates/.github/workflows/snyk-dotnet-scan.yml
        with:
          SnykScanEnabled: ${{ inputs.SnykScanEnabled }}
          SnykAuthToken: ${{ inputs.SnykAuthToken }}
          SnykSolutionFile: ${{ inputs.SolutionFile }}
          SnykOrg: ${{ inputs.SnykOrg }}
          SnykProjectName: ${{ inputs.SnykProjectName }}

      - name: Install Visual Studio Test Platform
        run: |
          echo "Installing Visual Studio Test Platform..."
          # Command to install the test platform

      - name: Run Build Tests
        if: ${{ inputs.VsTestEnabled }}
        run: |
          vstest.console.exe **\*test*.dll /EnableCodeCoverage
        working-directory: ${{ inputs.PathToTestProject }}

      - name: SonarQube Post-Build
        uses: ./templates/.github/workflows/sonarqube-post-build.yml
        with:
          SonarQubeProjectName: ${{ inputs.SonarQubeProjectName }}

      - name: Build Web Deploy Package
        run: msbuild ${{ inputs.PathToProject }} /p:Configuration=${{ inputs.BuildConfiguration }} /T:Package /P:OutputPath=$(Build.SourcesDirectory)\temp\ /P:PackageLocation=$(Build.SourcesDirectory)\Package\${{ inputs.ComponentName }}.zip

      - name: Copy Component Files for Publishing
        run: |
          mkdir -p $(Build.ArtifactStagingDirectory)/${{ inputs.ComponentName }}
          cp Package/${{ inputs.ComponentName }}.zip $(Build.ArtifactStagingDirectory)/${{ inputs.ComponentName }}
          cp temp/configuration.json $(Build.ArtifactStagingDirectory)/${{ inputs.ComponentName }}

      - name: Publish Build Artifact
        uses: actions/upload-artifact@v4
        with:
          name: ${{ inputs.ComponentName }}
          path: $(Build.ArtifactStagingDirectory)/${{ inputs.ComponentName }}
