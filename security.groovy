name: Setup MSBuild Internal
description: Sets up MSBuild on Windows agents
runs:
  using: composite
  steps:
    - name: Add MSBuild to PATH
      shell: powershell
      run: |
        Write-Host "Setting up MSBuild"
        $msbuildPath = "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\MSBuild\Current\Bin"
        if (Test-Path $msbuildPath) {
          echo "MSBuild found at: $msbuildPath"
          echo "$msbuildPath" | Out-File -FilePath $env:GITHUB_PATH -Encoding utf8 -Append
        } else {
          Write-Error "MSBuild not found in the expected location."
          exit 1
        }
