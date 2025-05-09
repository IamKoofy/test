name: build_and_push_nuget
description: Builds, versions, and pushes a .NET NuGet package

inputs:
  VersioningTaskGroupFilename:
    required: false
    default: task-groups/version-use-buildnumber.taskgroup.yml
  MajorVersion:
    required: true
  MinorVersion:
    required: true
  BuildNumber:
    required: true
  Rev:
    required: true
  AssemblyInfo:
    required: true
  SolutionFile:
    required: true
  BuildConfiguration:
    required: true
  BuildPlatform:
    required: true
  ProjectLocalPath:
    required: true
  NugetApiKey:
    required: true
  NugetSourceUrl:
    required: true

runs:
  using: composite
  steps:
    - name: Display Pipeline Info
      uses: AEGBT/github-actions-shared-lib/.github/actions/display-pipeline-info.taskgroup@main

    - name: Versioning
      uses: AEGBT/github-actions-shared-lib/.github/actions/version-use-buildnumber.taskgroup@main
      with:
        MajorVersion: ${{ inputs.MajorVersion }}
        MinorVersion: ${{ inputs.MinorVersion }}
        BuildNumber: ${{ inputs.BuildNumber }}
        Rev: ${{ inputs.Rev }}

    - name: Restore NuGet packages
      shell: powershell
      run: |
        nuget restore "${{ inputs.SolutionFile }}"

    - name: Update AssemblyInfo with BuildNumber
      shell: powershell
      run: |
        $file = "${{ github.workspace }}\${{ inputs.AssemblyInfo }}"
        attrib $file -r
        (Get-Content $file) -replace "1.0.0.1", "${{ inputs.BuildNumber }}" | Out-File $file
        Get-Content $file

    - name: Build Solution
      shell: powershell
      run: |
        dotnet build "${{ inputs.SolutionFile }}" `
          -c "${{ inputs.BuildConfiguration }}" `
          -p:Platform="${{ inputs.BuildPlatform }}"

    - name: Copy DLLs to staging
      shell: powershell
      run: |
        $src = "${{ inputs.ProjectLocalPath }}\**\bin\${{ inputs.BuildConfiguration }}\*.dll"
        $dst = "${{ github.workspace }}\staging\lib\Net40"
        New-Item -ItemType Directory -Force -Path $dst
        Copy-Item -Path $src -Destination $dst -Recurse -Force -Verbose

    - name: Copy Nuspec file
      shell: powershell
      run: |
        $src = "${{ inputs.ProjectLocalPath }}\package.nuspec"
        $dst = "${{ github.workspace }}\staging"
        Copy-Item -Path $src -Destination $dst -Force

    - name: Pack NuGet Package
      shell: powershell
      run: |
        $nuspec = "${{ github.workspace }}\staging\package.nuspec"
        $outDir = "${{ github.workspace }}\staging\nugetpackage"
        New-Item -ItemType Directory -Force -Path $outDir
        nuget pack $nuspec -OutputDirectory $outDir -Properties version="${{ inputs.BuildNumber }}"

    - name: Push NuGet Package
      shell: powershell
      run: |
        $package = Get-ChildItem "${{ github.workspace }}\staging\nugetpackage\*.nupkg" | Select-Object -First 1
        if (-not $package) {
          Write-Error "No NuGet package found to push."
          exit 1
        }
        dotnet nuget push $package.FullName `
          --source "${{ inputs.NugetSourceUrl }}" `
          --api-key "${{ inputs.NugetApiKey }}" `
          --skip-duplicate
