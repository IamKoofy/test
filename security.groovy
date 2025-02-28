param(
    [string]$DotnetVersion,
    [string]$SourceUrl
)

Write-Host "Setting up .NET SDK version: $DotnetVersion"

# Define the .NET install script path
$dotnetInstallScript = "$env:TEMP\dotnet-install.ps1"

# Download the .NET install script
Invoke-WebRequest -Uri "https://dot.net/v1/dotnet-install.ps1" -OutFile $dotnetInstallScript

# Install the specified .NET SDK version
& $dotnetInstallScript -Version $DotnetVersion

# Define the .NET installation path
$dotnetPath = "$env:USERPROFILE\.dotnet"

# Set DOTNET_ROOT environment variable
[System.Environment]::SetEnvironmentVariable("DOTNET_ROOT", $dotnetPath, [System.EnvironmentVariableTarget]::Machine)

# Add .NET to PATH
$env:Path = "$env:Path;$dotnetPath;$dotnetPath\tools"

Write-Host "Updated PATH: $env:Path"

# Set NuGet Source URL if provided
if ($SourceUrl -ne "") {
    Write-Host "Configuring NuGet Source: $SourceUrl"
    dotnet nuget add source $SourceUrl --name "internal-nuget"
}

Write-Host ".NET SDK setup completed successfully!"
