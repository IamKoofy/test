param(
    [string]$DotnetVersion,
    [string]$SourceUrl
)

Write-Host "Checking installed .NET SDK versions..."

$dotnetBasePath = "C:\Program Files\dotnet"
$dotnetSdkPath = "$dotnetBasePath\sdk"

if (!(Test-Path $dotnetSdkPath)) {
    Write-Host "Error: .NET SDK directory does not exist at $dotnetSdkPath"
    exit 1
}

$installedVersions = Get-ChildItem -Path $dotnetSdkPath | Select-Object -ExpandProperty Name

Write-Host "Installed .NET SDK versions:"
Write-Host $installedVersions

if ($installedVersions -contains $DotnetVersion) {
    Write-Host ".NET SDK version $DotnetVersion is installed."
} else {
    Write-Host "Error: .NET SDK version $DotnetVersion is NOT installed on this agent."
    exit 1
}

$env:Path = "$dotnetBasePath;$env:Path"
[System.Environment]::SetEnvironmentVariable("DOTNET_ROOT", $dotnetBasePath, [System.EnvironmentVariableTarget]::Process)

Write-Host "Updated PATH: $env:Path"

$globalJsonPath = "$env:GITHUB_WORKSPACE\global.json"
$jsonContent = "{""sdk"": {""version"": ""$DotnetVersion""}}"
$jsonContent | Out-File -FilePath $globalJsonPath -Encoding utf8

Write-Host "Created global.json to use .NET SDK $DotnetVersion"

$activeVersion = & "$dotnetBasePath\dotnet.exe" --version
Write-Host "Active .NET SDK version: $activeVersion"

if ($activeVersion -like "$DotnetVersion*") {
    Write-Host "Correct .NET SDK version is active."
} else {
    Write-Host "Warning: The active .NET SDK version ($activeVersion) does not match the expected version ($DotnetVersion)."
}

if ($SourceUrl -ne "") {
    Write-Host "Configuring NuGet Source: $SourceUrl"
    & "$dotnetBasePath\dotnet.exe" nuget add source $SourceUrl --name "internal-nuget"
}

Write-Host ".NET SDK setup completed successfully."
