    - name: Upload artifact using GitHub API
      shell: powershell
      run: |
        $ARTIFACT_NAME = "${{ inputs.name }}"
        $ARTIFACT_PATH = "$env:GITHUB_WORKSPACE\artifacts\${{ inputs.name }}.zip"
        
        Write-Host "Uploading artifact $ARTIFACT_NAME..."
        
        Invoke-RestMethod -Uri "https://api.github.com/repos/${{ github.repository }}/actions/artifacts" `
          -Method Post `
          -Headers @{
            Authorization = "Bearer ${{ inputs.token }}"
            Accept = "application/vnd.github.v3+json"
          } `
          -Form @{
            name = $ARTIFACT_NAME
            file = Get-Item -Path $ARTIFACT_PATH
          }
