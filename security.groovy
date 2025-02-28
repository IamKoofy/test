name: "Setup .NET (Internal)"
description: "Sets up the required .NET SDK version"

inputs:
  dotnet-version:
    description: "The .NET SDK version to use"
    required: true
  source-url:
    description: "NuGet package source URL"
    required: false

runs:
  using: "composite"
  steps:
    - name: Debug Workspace
      shell: pwsh
      run: |
        Write-Host "Current Directory: $PWD"
        Get-ChildItem -Path $env:GITHUB_WORKSPACE -Recurse

    - name: Ensure .NET SDK is Installed
      shell: pwsh
      run: |
        $dotnetVersion = "${{ inputs.dotnet-version }}"
        $installedVersions = & dotnet --list-sdks | ForEach-Object { ($_ -split ' ')[0] }

        if ($installedVersions -contains $dotnetVersion) {
          Write-Host ".NET SDK version $dotnetVersion is already installed."
        } else {
          Write-Host "Error: .NET SDK version $dotnetVersion is not installed on this agent."
          exit 1
        }

    - name: Set .NET Environment Variables
      shell: pwsh
      run: |
        $dotnetPath = "C:\Program Files\dotnet"
        [System.Environment]::SetEnvironmentVariable("DOTNET_ROOT", $dotnetPath, [System.EnvironmentVariableTarget]::Machine)
        $env:Path += ";$dotnetPath;$dotnetPath\tools"

    - name: Configure NuGet Source (if provided)
      if: inputs.source-url != ''
      shell: pwsh
      run: |
        Write-Host "Configuring NuGet Source: ${{ inputs.source-url }}"
        dotnet nuget add source "${{ inputs.source-url }}" --name "internal-nuget"
