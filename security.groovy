name: "Setup MSBuild (Internal)"
description: "Sets up MSBuild on Windows agents"
runs:
  using: "composite"
  steps:
    - name: Setup MSBuild
      shell: pwsh
      run: ./github/actions/setup-msbuild/setup-msbuild.ps1



Write-Host "🔹 Setting up MSBuild..."

# Locate the latest Visual Studio installation
$vswherePath = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe"

if (Test-Path $vswherePath) {
    $vsPath = & $vswherePath -latest -products * -requires Microsoft.Component.MSBuild -property installationPath
    if ($vsPath) {
        $msbuildPath = "$vsPath\MSBuild\Current\Bin\MSBuild.exe"
        if (Test-Path $msbuildPath) {
            Write-Host "✅ MSBuild found at: $msbuildPath"
            [System.Environment]::SetEnvironmentVariable("MSBUILD_PATH", $msbuildPath, [System.EnvironmentVariableTarget]::Machine)
            $env:Path += ";$msbuildPath"
        } else {
            Write-Error "❌ MSBuild was not found in the expected location."
            exit 1
        }
    } else {
        Write-Error "❌ Visual Studio installation with MSBuild not found."
        exit 1
    }
} else {
    Write-Error "❌ vswhere.exe not found. Please install Visual Studio."
    exit 1
}

Write-Host "✅ MSBuild setup completed!"
