- name: Set .NET Environment Variables
  shell: powershell
  run: |
    $dotnetPath = "C:\Program Files\dotnet"
    [System.Environment]::SetEnvironmentVariable("DOTNET_ROOT", $dotnetPath, [System.EnvironmentVariableTarget]::User)
    Write-Host "DOTNET_ROOT set for the user.
