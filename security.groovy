$reportPath = ".sonarqube\out\report-task.txt"
    $targetPath = "$env:RUNNER_TEMP\report-task.txt"

    if (Test-Path $reportPath) {
      Copy-Item $reportPath $targetPath
      Write-Host "Copied report to temp location: $targetPath"
    } else {
      Write-Warning "report-task.txt not found"
    }
