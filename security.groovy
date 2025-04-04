steps:
  - name: List Files and Folders
    shell: pwsh
    run: |
      Write-Host "Listing files and folders in the current directory:"
      Get-ChildItem -Path . | Format-List Name, Mode, LastWriteTime
