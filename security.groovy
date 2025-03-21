- task: PowerShell@2
  displayName: Print All Files in Workspace
  inputs:
    targetType: inline
    workingDirectory: '$(Build.SourcesDirectory)'
    script: |
      Write-Host "Listing all files in the workspace:"
      Get-ChildItem -Recurse | ForEach-Object { Write-Host $_.FullName }
