- name: Configure Nexus NuGet Source (Anonymous)
  shell: powershell
  run: |
    $sourceName = "nexus-nuget"
    $sourceUrl = "https://nuget.repo.com/repository/nuget-group"
    $existingSources = dotnet nuget list source
    if ($existingSources -match $sourceUrl) {
      Write-Host "NuGet source already exists: $sourceUrl"
    } else {
      dotnet nuget add source --name $sourceName $sourceUrl
      Write-Host "Added Nexus NuGet source: $sourceUrl"
    }
