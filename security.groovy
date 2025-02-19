name: "Run VSTest (Internal)"
description: "Runs VSTest for .NET projects"
inputs:
  testAssembly:
    description: "Pattern to find test assemblies"
    required: true
  searchFolder:
    description: "Folder where test assemblies are located"
    required: true
  vsTestVersion:
    description: "Version of VSTest to use"
    required: false
    default: "toolsInstaller"
  codeCoverageEnabled:
    description: "Enable code coverage"
    required: false
    default: "true"
  platform:
    description: "Target platform"
    required: false
    default: "Any CPU"
runs:
  using: "composite"
  steps:
    - name: Run VSTest
      shell: pwsh
      run: |
        ./github/actions/vstest/run-vstest.ps1 `
          -TestAssembly "${{ inputs.testAssembly }}" `
          -SearchFolder "${{ inputs.searchFolder }}" `
          -VsTestVersion "${{ inputs.vsTestVersion }}" `
          -CodeCoverageEnabled "${{ inputs.codeCoverageEnabled }}" `
          -Platform "${{ inputs.platform }}"










param(
    [string]$TestAssembly,
    [string]$SearchFolder,
    [string]$VsTestVersion = "toolsInstaller",
    [string]$CodeCoverageEnabled = "true",
    [string]$Platform = "Any CPU"
)

Write-Host "🔹 Running VSTest with the following parameters:"
Write-Host "Test Assembly: $TestAssembly"
Write-Host "Search Folder: $SearchFolder"
Write-Host "VSTest Version: $VsTestVersion"
Write-Host "Code Coverage Enabled: $CodeCoverageEnabled"
Write-Host "Platform: $Platform"

# Install VSTest if not installed
Write-Host "🔹 Installing VSTest..."
if (-Not (Get-Command vstest.console.exe -ErrorAction SilentlyContinue)) {
    Install-Package Microsoft.TestPlatform -Source "https://repos.gbt.gbtad.com/repository/nuget-api-v3/index.json"
}

# Run tests
Write-Host "🔹 Running Tests..."
$command = "vstest.console.exe `"$SearchFolder\$TestAssembly`" /Platform:$Platform"
if ($CodeCoverageEnabled -eq "true") {
    $command += " /EnableCodeCoverage"
}

Invoke-Expression $command
