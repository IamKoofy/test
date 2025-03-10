- name: Version the Application
      shell: pwsh
      run: |
        $buildNumber = "${{ inputs.BuildNumber }}"
        $rev = "${{ inputs.Rev }}"
        
        if ($buildNumber.Trim().ToLower() -eq 'auto') {
          $year = (Get-Date -Format "yy")
          $dayNumber = (Get-Date).DayOfYear.ToString().PadLeft(3, '0')
          $buildNumber = "$year$dayNumber"
        }

        if ($rev.Trim().ToLower() -eq 'auto') {
          $rev = "${{ github.run_number }}"
        }

        $newBuildNumber = "${{ inputs.MajorVersion }}.${{ inputs.MinorVersion }}.$buildNumber.$rev"
        Write-Host "New Build Number: $newBuildNumber"
