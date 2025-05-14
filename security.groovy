- name: Ensure clean NuGet source
  shell: pwsh
  run: |
    if (dotnet nuget list source | Select-String "hrgtec") {
      dotnet nuget remove source "hrgtec"
    }
