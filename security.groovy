param(
    [string]$DotnetVersion,
    [string]$SourceUrl
)

Write-Host "Checking if .NET SDK version $DotnetVersion is installed..."

# Get the list of installed .NET SDK versions
$dotnetPath = "$env:ProgramFiles\dotnet"
$installedVersions = Get-ChildItem -Path "$dotnetPath\sdk" -Name

if ($installedVersions -contains $DotnetVersion) {
    Write-Host ".NET SDK version $DotnetVersion is installed."
} else {
    Write-Host "Error: .NET SDK version $DotnetVersion is not installed on this agent."
    exit 1
}

# Set DOTNET_ROOT and update PATH
[System.Environment]::SetEnvironmentVariable("DOTNET_ROOT", $dotnetPath, [System.EnvironmentVariableTarget]::Process)
$env:Path = "$dotnetPath;$dotnetPath\tools;$env:Path"

Write-Host "Updated PATH: $env:Path"

# Set Global JSON for correct SDK selection
$globalJsonPath = "$env:GITHUB_WORKSPACE\global.json"
$jsonContent = "{""sdk"": {""version"": ""$DotnetVersion""}}"
$jsonContent | Out-File -FilePath $globalJsonPath -Encoding utf8

Write-Host "Created global.json to use .NET SDK $DotnetVersion"

# Verify the active .NET version
$activeVersion = & "$dotnetPath\dotnet.exe" --version
Write-Host "Active .NET SDK version: $activeVersion"

if ($activeVersion -like "$DotnetVersion*") {
    Write-Host "Correct .NET SDK version is active."
} else {
    Write-Host "Warning: The active .NET SDK version ($activeVersion) does not match the expected version ($DotnetVersion)."
}

# Set NuGet Source URL if provided
if ($SourceUrl -ne "") {
    Write-Host "Configuring NuGet Source: $SourceUrl"
    dotnet nuget add source $SourceUrl --name "internal-nuget"
}

Write-Host ".NET SDK setup completed successfully."
