    - name: Publish SonarQube Report Summary
      shell: powershell
      run: |
        $reportPath = ".sonarqube\out\report-task.txt"
        if (-Not (Test-Path $reportPath)) {
            Write-Error "SonarQube report-task.txt not found. Skipping report publish."
            exit 1
        }

        $report = Get-Content $reportPath | ConvertFrom-StringData
        $ceTaskUrl = $report["ceTaskUrl"]
        $analysisId = ""
        $status = ""

        Write-Host "Waiting for SonarQube analysis task to complete..."
        for ($i = 0; $i -lt 30; $i++) {
            Start-Sleep -Seconds 5
            $response = Invoke-RestMethod -Uri $ceTaskUrl -Headers @{ "Authorization" = "Basic $([Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('${{ inputs.sonarqube_token }}:')))" }

            $status = $response.task.status
            if ($status -eq "SUCCESS" -or $status -eq "FAILED" -or $status -eq "CANCELED") {
                $analysisId = $response.task.analysisId
                break
            }
        }

        if (-not $analysisId) {
            Write-Error "Analysis did not complete in time."
            exit 1
        }

        $analysisResultUrl = "${{ inputs.sonarqube_url }}/api/qualitygates/project_status?analysisId=$analysisId"
        $result = Invoke-RestMethod -Uri $analysisResultUrl -Headers @{ "Authorization" = "Basic $([Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('${{ inputs.sonarqube_token }}:')))" }

        $status = $result.projectStatus.status
        Write-Host "Quality Gate Status: $status"

        if ($status -ne "OK") {
            Write-Warning "SonarQube Quality Gate failed with status: $status"
        }
