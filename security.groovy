param(
    [string]$DotnetVersion,
    [string]$SourceUrl
)

Write-Host "Checking for .NET SDK version requirements..."

# Set base paths
$dotnetBasePath = "C:\Program Files\dotnet"
$dotnetSdkPath = "$dotnetBasePath\sdk"
$globalJsonPath = "$env:GITHUB_WORKSPACE\global.json"

# Check if global.json exists
if (Test-Path $globalJsonPath) {
    Write-Host "Found global.json, using specified .NET SDK version."
    $globalJson = Get-Content -Raw -Path $globalJsonPath | ConvertFrom-Json
    $DotnetVersion = $globalJson.sdk.version
    Write-Host "Detected SDK version from global.json: $DotnetVersion"
}
elseif (Test-Path "$env:GITHUB_WORKSPACE\*.csproj") {
    Write-Host "No global.json found. Checking .csproj for required .NET version..."
    $csprojFile = Get-ChildItem -Path "$env:GITHUB_WORKSPACE" -Filter "*.csproj" | Select-Object -First 1
    if ($csprojFile) {
        $targetFramework = Select-String -Path $csprojFile.FullName -Pattern "<TargetFramework>(.+)</TargetFramework>" | ForEach-Object { $_.Matches.Groups[1].Value }
        if ($targetFramework -match "net(\d+)\.(\d+)") {
            $DotnetVersion = "$($matches[1]).$($matches[2])"
            Write-Host "Detected SDK version from .csproj: $DotnetVersion"
        }
    }
}

# Ensure we have a valid .NET SDK version
if (-not $DotnetVersion) {
    Write-Host "Error: Unable to determine .NET SDK version. Please specify in global.json or .csproj."
    exit 1
}

# Check installed .NET SDK versions
Write-Host "Checking installed .NET SDK versions..."
if (!(Test-Path $dotnetSdkPath)) {
    Write-Host "Error: .NET SDK directory not found at $dotnetSdkPath"
    exit 1
}

$installedVersions = Get-ChildItem -Path $dotnetSdkPath | Select-Object -ExpandProperty Name
Write-Host "Installed .NET SDK versions:"
$installedVersions | ForEach-Object { Write-Host $_ }

# Verify required version is installed
if ($installedVersions -match "^$DotnetVersion") {
    Write-Host "Required .NET SDK version $DotnetVersion is installed."
} else {
    Write-Host "Error: .NET SDK version $DotnetVersion is NOT installed on this agent."
    exit 1
}

# Set environment variables
$env:Path = "$dotnetBasePath;$env:Path"
[System.Environment]::SetEnvironmentVariable("DOTNET_ROOT", $dotnetBasePath, [System.EnvironmentVariableTarget]::Process)

Write-Host "Updated PATH: $env:Path"

# Set up global.json for consistency
$jsonContent = "{""sdk"": {""version"": ""$DotnetVersion""}}"
$jsonContent | Out-File -FilePath $globalJsonPath -Encoding utf8
Write-Host "Created global.json to use .NET SDK $DotnetVersion"

# Verify active SDK version
$activeVersion = & "$dotnetBasePath\dotnet.exe" --version
Write-Host "Active .NET SDK version: $activeVersion"

if ($activeVersion -match "^$DotnetVersion") {
    Write-Host "Correct .NET SDK version is active."
} else {
    Write-Host "Warning: The active .NET SDK version ($activeVersion) does not match the expected version ($DotnetVersion)."
}

# Configure NuGet source if provided
if ($SourceUrl -ne "") {
    Write-Host "Configuring NuGet Source: $SourceUrl"
    & "$dotnetBasePath\dotnet.exe" nuget add source $SourceUrl --name "internal-nuget"
}

Write-Host ".NET SDK setup completed successfully."
