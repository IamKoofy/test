name: "SonarQube End & Check"
description: "Runs SonarScanner end and waits for quality gate result"
inputs:
  sonar-token:
    description: "SonarQube token"
    required: true
runs:
  using: "composite"
  steps:
    - name: Run Sonar End and Extract Task ID
      id: sonar_end
      shell: pwsh
      run: |
        $ErrorActionPreference = "Stop"

        Write-Host "Running SonarScanner end step..."
        $sonarOutput = dotnet sonarscanner end /d:sonar.login="${{ inputs.sonar-token }}" 2>&1

        $sonarOutput | ForEach-Object { Write-Host $_ }

        $taskIdPattern = "https:\/\/codequality\.myorg\.com\/api\/ce\/task\?id=([a-zA-Z0-9\-_]+)"
        if ($sonarOutput -match $taskIdPattern) {
          $ceTaskId = $Matches[1]
          Write-Host "Found ceTaskId: $ceTaskId"
          echo "ceTaskId=$ceTaskId" >> $env:GITHUB_OUTPUT
        } else {
          Write-Error "Could not extract ceTaskId from scanner output."
          exit 1
        }

    - name: Poll for SonarQube Task Completion
      shell: pwsh
      run: |
        $ErrorActionPreference = "Stop"
        $ceTaskId = "${{ steps.sonar_end.outputs.ceTaskId }}"
        $apiUrl = "https://codequality.myorg.com/api/ce/task?id=$ceTaskId"

        $authHeader = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${{ inputs.sonar-token }}:"))

        $maxRetries = 10
        $waitSeconds = 10

        for ($i = 0; $i -lt $maxRetries; $i++) {
          Start-Sleep -Seconds $waitSeconds
          try {
            $response = Invoke-RestMethod -Uri $apiUrl -Headers @{ "Authorization" = $authHeader }
            $status = $response.task.status
            Write-Host "Task status: $status"
            if ($status -eq "SUCCESS") {
              Write-Host "SonarQube background task completed."
              break
            } elseif ($status -eq "FAILED") {
              Write-Error "SonarQube background task failed."
              exit 1
            } else {
              Write-Host "Waiting... ($i/$maxRetries)"
            }
          } catch {
            Write-Warning "Error polling SonarQube task: $_"
          }
        }
