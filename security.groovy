parameters:
  - name: MajorVersion
    type: string
  - name: MinorVersion
    type: string
  - name: BuildNumber
    type: string
  - name: Rev
    type: string

steps:
  - task: PowerShell@2
    displayName: 'Version Build'
    inputs:
      targetType: 'inline'
      script: |
        Write-Host "Versioning based on the following:"
        Write-Host "Major: ${{ parameters.MajorVersion }}, Minor: ${{ parameters.MinorVersion }}, Build: ${{ parameters.BuildNumber }}"

        $majorVersion = "${{ parameters.MajorVersion }}"
        $minorVersion = "${{ parameters.MinorVersion }}"
        $buildNumber = "${{ parameters.BuildNumber }}"

        # Update the version using NBGV
        .\nbgv cloud --version "$majorVersion.$minorVersion.$buildNumber"

        # Update .NET assemblies
        Write-Host "Searching for .NET assemblies..."
        $files = Get-ChildItem $(Build.SourcesDirectory) -Recurse -Include @("*Properties*", "*My Project*") | 
                 Where-Object { $_.PSIsContainer } | 
                 ForEach-Object { Get-ChildItem -Path $_.FullName -Recurse -Include AssemblyInfo.* }

        if ($files.Count -gt 0) {
          Write-Host "Applying $env:BUILD_BUILDNUMBER to the following file(s):"
          foreach ($file in $files) {
            $filecontent = Get-Content($file.FullName)
            Set-ItemProperty -Path $file.FullName -Name IsReadOnly -Value $false
            $filecontent -replace "\d+\.\d+\.\d+\.\d+", "$env:BUILD_BUILDNUMBER" | Out-File $file.FullName
            Write-Host "`t$file.FullName"
          }
        } else {
          Write-Host "No files found."
        }
    workingDirectory: 'D:\BuildAgents\1\_work\_temp\data-test'
