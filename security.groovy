- name: Load Pipeline Variables
  id: load-vars
  shell: pwsh
  run: |
    Write-Host "Loading pipeline variables from pipeline-vars.yml"
    $yamlPath = "$env:GITHUB_WORKSPACE\pipeline-vars.yml"

    if (Test-Path $yamlPath) {
      $content = Get-Content $yamlPath | Where-Object {$_ -match '^\s*[^#]'} # Ignore comments
      foreach ($line in $content) {
        $key, $value = $line -split ":\s*", 2
        if ($key -and $value) {
          echo "$key=$value" | Out-File -Append -Encoding utf8 $env:GITHUB_ENV
        }
      }
    } else {
      Write-Error "pipeline-vars.yml not found!"
      exit 1
    }
