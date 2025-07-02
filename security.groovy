- task: PowerShell@2
  displayName: 'Force kill AppPool worker processes'
  inputs:
    targetType: inline
    script: |
      $appPoolName = "$(ComponentSiteName)"
      Write-Host "Checking processes for AppPool: $appPoolName"
      Import-Module WebAdministration

      # Get worker processes for app pool
      $workerProcesses = Get-WmiObject -Namespace "root\WebAdministration" -Class "WorkerProcess" | Where-Object { $_.AppPoolName -eq $appPoolName }

      foreach ($wp in $workerProcesses) {
          $pid = $wp.ProcessId
          try {
              Write-Host "Killing process $pid"
              Stop-Process -Id $pid -Force
          } catch {
              Write-Warning "Failed to kill process $pid: $_"
          }
      }

      # Double-check any dotnet.exe still locking files (optional)
      Get-Process | Where-Object { $_.ProcessName -like "dotnet" } | ForEach-Object {
          Write-Host "Checking dotnet process: $($_.Id)"
      }
