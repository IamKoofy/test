name: templates_build_hrg_dotnet_framework

inputs:
  MajorVersion:
    required: false
    type: string
  MinorVersion:
    required: false
    type: string
  BuildNumber:
    required: false
    type: string
  Rev:
    required: false
    type: string
  SolutionFile:
    required: false
    type: string
  PathToProject:
    required: false
    type: string
  PathToTestProject:
    required: false
    type: string
  ComponentName:
    required: false
    type: string
  BuildConfiguration:
    required: false
    type: string
  SnykScanEnabled:
    required: false
    default: true
    type: boolean
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
    default: GBT Code Analysis
    type: string
  VsTestEnabled:
    required: false
    default: false
    type: boolean

runs:
  using: composite
  steps:
    - uses: "./.github/actions/templates_common_task_groups_display_pipeline_info"

    - uses: "./.github/actions/templates_build_task_groups_version_use_buildnumbertaskgroup"
      with:
        MajorVersion: "${{ inputs.MajorVersion }}"
        MinorVersion: "${{ inputs.MinorVersion }}"
        BuildNumber: "${{ inputs.BuildNumber }}"
        Rev: "${{ inputs.Rev }}"

    - uses: "./.github/actions/templates_build_task_groups_sonarqube_pre_build_vstesttaskgroup"
      with:
        SonarQubeProjectName: "${{ inputs.SonarQubeProjectName }}"
        SonarQubeExclusions: "${{ inputs.SonarQubeExclusions }}"
        SonarQubeServiceConnection: "${{ inputs.SonarQubeServiceConnection }}"

    - name: Authenticate with NuGet feed
      uses: actions/setup-dotnet@v4
      env:
        NUGET_AUTH_TOKEN: "${{ secrets.NUGET_AUTH_TOKEN }}"
      with:
        source-url: "https://repos.gbt.gbtad.com/repository/nuget-api-v3/index.json"

    - name: Install Visual Studio Test Platform
      if: inputs.VsTestEnabled == 'true'
      shell: bash
      run: nuget install Microsoft.TestPlatform -Source "https://repos.gbt.gbtad.com/repository/nuget-api-v3/index.json"

    - name: Restore NuGet packages
      shell: bash
      run: nuget restore ${{ inputs.SolutionFile }}

    - name: Install MSBuild
      uses: microsoft/setup-msbuild@v1.3.1

    - name: Run MSBuild
      shell: bash
      run: msbuild "${{ github.workspace }}/${{ inputs.SolutionFile }}" -p:Configuration=${{ inputs.BuildConfiguration }} -p:Platform="Any CPU"

    - uses: "./.github/actions/templates_build_task_groups_snyk_dotnet_scantaskgroup"
      with:
        SnykScanEnabled: "${{ inputs.SnykScanEnabled }}"
        SnykSolutionFile: "${{ inputs.SolutionFile }}"
        SnykAuthToken: "${{ inputs.SnykAuthToken }}"
        SnykOrg: "${{ inputs.SnykOrg }}"
        SnykProjectName: "${{ inputs.SnykProjectName }}"

    - name: Run build tests
      if: success() && inputs.VsTestEnabled == 'true'
      uses: microsoft/vstest-action@v1.0.0
      with:
        testAssembly: |
          **\*test*.dll
          !**\Microsoft.*.dll
          !**\*TestAdapter.dll
          !**\obj\**
        searchFolder: "${{ inputs.PathToTestProject }}"
        vsTestVersion: toolsInstaller
        codeCoverageEnabled: true
        platform: "Any CPU"

    - uses: "./.github/actions/sonarqube-post-build.taskgroup"
      with:
        SonarQubeProjectName: "${{ inputs.SonarQubeProjectName }}"

    - name: Package Project
      shell: bash
      run: |
        msbuild "${{ github.workspace }}/${{ inputs.PathToProject }}" /T:Package \
          /P:OutputPath=${{ github.workspace }}/temp/ \
          /P:PackageLocation=${{ github.workspace }}/Package/${{ inputs.ComponentName }}.zip \
          -p:Configuration=${{ inputs.BuildConfiguration }} \
          -p:Platform="Any CPU"

    - name: Publish Artifact
      uses: actions/upload-artifact@v4
      with:
        name: "${{ inputs.ComponentName }}"
        path: "${{ github.workspace }}/Package/${{ inputs.ComponentName }}.zip"
