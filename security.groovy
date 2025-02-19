name: "Setup .NET (Internal)"
description: "Sets up the required .NET SDK version"
inputs:
  dotnet-version:
    description: "The .NET SDK version to install"
    required: true
  source-url:
    description: "NuGet package source URL"
    required: false
runs:
  using: "composite"
  steps:
    - name: Install .NET SDK
      shell: pwsh
      run: |
        ./github/actions/setup-dotnet/setup-dotnet.ps1 `
          -DotnetVersion "${{ inputs.dotnet-version }}" `
          -SourceUrl "${{ inputs.source-url }}"





param(
    [string]$DotnetVersion,
    [string]$SourceUrl
)

Write-Host "🔹 Setting up .NET SDK version: $DotnetVersion"

# Install .NET SDK
$dotnetInstallScript = "$env:TEMP\dotnet-install.ps1"
Invoke-WebRequest -Uri "https://dot.net/v1/dotnet-install.ps1" -OutFile $dotnetInstallScript
& $dotnetInstallScript -Version $DotnetVersion

# Add .NET to PATH
$dotnetPath = "$env:USERPROFILE\.dotnet"
[System.Environment]::SetEnvironmentVariable("DOTNET_ROOT", $dotnetPath, [System.EnvironmentVariableTarget]::Machine)
$env:Path += ";$dotnetPath;$dotnetPath\tools"

# Set NuGet Source URL if provided
if ($SourceUrl -ne "") {
    Write-Host "🔹 Configuring NuGet Source: $SourceUrl"
    dotnet nuget add source $SourceUrl --name "internal-nuget"
}

Write-Host "✅ .NET SDK setup completed!"
