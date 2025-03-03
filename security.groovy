- name: Package Project
  shell: powershell
  run: |
    $projectPath = "${{ github.workspace }}\${{ inputs.PathToProject }}"
    $outputPath = "${{ github.workspace }}\temp"
    $packageLocation = "${{ github.workspace }}\Package\${{ inputs.ComponentName }}.zip"
    $buildConfig = "${{ inputs.BuildConfiguration }}"
    
    msbuild $projectPath /T:Package /P:OutputPath=$outputPath /P:PackageLocation=$packageLocation /p:Configuration=$buildConfig /p:Platform="Any CPU"
