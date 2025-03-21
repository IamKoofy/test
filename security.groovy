- task: PowerShell@2
    displayName: Verify node_modules
    inputs:
      targetType: inline
      workingDirectory: '$(Build.SourcesDirectory)\gbc-travel-hero'
      script: |
        if (Test-Path "node_modules") {
          Write-Host "node_modules successfully installed."
        } else {
          Write-Error "node_modules directory missing after install!"
          exit 1
        }
