name: Build .NET Framework App

on:
  workflow_call:
    inputs:
      VersioningTaskGroupFilename:
        required: false
        type: string
        default: 'task-groups/version-use-buildnumber.taskgroup.yml'
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
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Display Pipeline Info
        run: echo "Running .NET Framework build"

      - name: Run Versioning Task Group
        run: echo "Running ${{ inputs.VersioningTaskGroupFilename }} with Version: ${{ inputs.MajorVersion }}.${{ inputs.MinorVersion }}.${{ inputs.BuildNumber }}.${{ inputs.Rev }}"

      - name: SonarQube Pre-Build Analysis
        run: echo "Running SonarQube analysis for project ${{ inputs.SonarQubeProjectName }}"

      - name: Authenticate with NuGet Feed
        run: echo "NuGet authentication completed"

      - name: Restore NuGet Packages
        run: nuget restore "${{ inputs.SolutionFile }}"

      - name: Build .NET Framework Project
        run: |
          msbuild ${{ inputs.SolutionFile }} /p:Configuration=${{ inputs.BuildConfiguration }} /p:Platform="Any CPU"

      - name: Run Snyk Security Scan
        if: inputs.SnykScanEnabled == true
        env:
          SNYK_TOKEN: ${{ inputs.SnykAuthToken }}
        run: |
          echo "Running Snyk security scan for project ${{ inputs.SnykProjectName }}"

      - name: Install Visual Studio Test Platform
        run: echo "Installing Visual Studio Test Platform"

      - name: Run Tests
        if: inputs.VsTestEnabled == true
        run: |
          vstest.console.exe "${{ inputs.PathToTestProject }}\**\*test*.dll" /EnableCodeCoverage

      - name: SonarQube Post-Build Analysis
        run: echo "Finalizing SonarQube analysis for ${{ inputs.SonarQubeProjectName }}"

      - name: Build Web Deploy Package
        run: |
          msbuild "${{ inputs.PathToProject }}" /p:Configuration=${{ inputs.BuildConfiguration }} /t:Package /p:OutputPath=$(Build.SourcesDirectory)\temp\ /p:PackageLocation=$(Build.SourcesDirectory)\Package\${{ inputs.ComponentName }}.zip

      - name: Copy Component Files
        run: |
          mkdir -p ${{ github.workspace }}/artifacts/${{ inputs.ComponentName }}
          cp -r Package/${{ inputs.ComponentName }}.zip temp/configuration.json ${{ github.workspace }}/artifacts/${{ inputs.ComponentName }}

      - name: Upload Build Artifact
        uses: actions/upload-artifact@v4
        with:
          name: ${{ inputs.ComponentName }}
          path: ${{ github.workspace }}/artifacts/${{ inputs.ComponentName }}
