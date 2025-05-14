- name: Ensure clean NuGet source by URL
  shell: pwsh
  run: |
    $sourceUrl = "https://hrgtec.pkgs.visualstudio.com/_packaging/hrgtec/nuget/v3/index.json"
    $sources = dotnet nuget list source
    $sourceName = $null

    for ($i = 0; $i -lt $sources.Count; $i++) {
      $line = $sources[$i].Trim()

      # Fix: Proper parentheses to group regex options
      if ($line -match '^\d+\.\s+(.+?)\s+\[(Enabled|Disabled)\]') {
        $potentialName = $Matches[1].Trim()
        $nextLine = $sources[$i + 1].Trim()
        if ($nextLine -eq $sourceUrl) {
          $sourceName = $potentialName
          break
        }
      }
    }

    if ($sourceName) {
      Write-Host "Removing existing NuGet source: $sourceName"
      dotnet nuget remove source "$sourceName"
    } else {
      Write-Host "No matching NuGet source to remove."
    }

    dotnet nuget add source `
      --name "hrgtec" `
      --username "buildagent" `
      --password "${{ inputs.azure-devops-pat }}" `
      --store-password-in-clear-text `
      $sourceUrl
