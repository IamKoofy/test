name: Versioning Build Number

on:
  workflow_call:
    inputs:
      MajorVersion:
        required: true
        type: string
      MinorVersion:
        required: true
        type: string
      BuildNumber:
        required: true
        type: string
      Rev:
        required: true
        type: string
    secrets: {}  # If needed

    # Allow the caller workflow to specify a runner
    outputs:
      runner:
        description: "The runner to use"
        value: ${{ inputs.runner }}

jobs:
  versioning:
    runs-on: ${{ inputs.runner }}  # Caller must provide a runner
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Version Build
        shell: pwsh
        run: |
          Write-Host "Versioning based on the following:"
          Write-Host "${{ inputs.BuildNumber }}"
          $versionRegex = "\d+\.\d+\.\d+\.\d+"

          # Update .NET assemblies
          Write-Host "Searching for .NET assemblies..."
          $files = Get-ChildItem -Path . -Recurse -Include @("*Properties*", "*My Project*") | Where-Object {$_.PSIsContainer} | ForEach-Object {
              Get-ChildItem -Path $_.FullName -Recurse -Include AssemblyInfo.*
          }

          if ($files.Count -gt 0) {
              Write-Host "Applying ${{ inputs.BuildNumber }} to the following files:"
              foreach ($file in $files) {
                  $fileContent = Get-Content $file
                  attrib $file -r
                  $fileContent -replace $versionRegex, "${{ inputs.BuildNumber }}" | Set-Content $file
                  Write-Host "`t$file"
              }
          } else {
              Write-Host "No files found."
          }
