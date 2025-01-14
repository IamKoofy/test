- task: PowerShell@1
  displayName: 'Remove Existing Windows Service'
  inputs:
    scriptType: 'inlineScript'
    script: |
      $serviceName = "$(ComponentName)"
      
      # Check if the service exists
      if (Get-Service -Name $serviceName -ErrorAction SilentlyContinue) {
          Write-Host "Service $serviceName exists. Stopping and removing it..."
          Stop-Service -Name $serviceName -Force -ErrorAction SilentlyContinue
          sc.exe delete $serviceName
          Write-Host "Service $serviceName has been removed."
      } else {
          Write-Host "Service $serviceName does not exist. Skipping removal."
