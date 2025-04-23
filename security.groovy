Get-ChildItem -Recurse -Directory -Force | Where-Object { $_.Name -eq ".sonarqube" }
