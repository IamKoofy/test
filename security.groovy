 - name: List files in .sonarqube\out directory
        shell: pwsh
        run: |
          $sonarOutDir = ".sonarqube\out"
          
          # Check if the directory exists
          if (Test-Path $sonarOutDir) {
              Write-Host "Directory exists: $sonarOutDir"
              
              # List all files in the directory
              Write-Host "Listing files in $sonarOutDir:"
              Get-ChildItem -Path $sonarOutDir
          } else {
              Write-Host "Directory does not exist: $sonarOutDir"
          }
