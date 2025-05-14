- name: Ensure clean NuGet source by URL
  shell: pwsh
  run: |
    $sourceUrl = "https://hrgtec.pkgs.visualstudio.com/_packaging/hrgtec/nuget/v3/index.json"
    $existingSourceName = dotnet nuget list source | ForEach-Object {
      if ($_ -match "^\s*(\*?)\s*([^\s]+)\s+\[$sourceUrl\]") {
        return $matches[2]
      }
    }

    if ($existingSourceName) {
      Write-Host "Removing existing NuGet source: $existingSourceName"
      dotnet nuget remove source $existingSourceName
    }
