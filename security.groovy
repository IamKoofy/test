task-groups/version-use-buildnumber.taskgroup.yml:

- name: MajorVersion
  type: string

- name: MinorVersion
  type: string

- name: BuildNumber
  type: string

# Rev is no longer needed since we won't include it
#- name: Rev
#  type: string

steps:

- task: PowerShell@2
  displayName: 'Version Build'
  inputs:
    targetType: 'inline'
    script: |
      Write-Host "Versioning based on the following:-"
      Write-Host "MajorVersion: $(MajorVersion)"
      Write-Host "MinorVersion: $(MinorVersion)"
      Write-Host "BuildNumber: $(BuildNumber)"

      $version = "$(MajorVersion).$(MinorVersion).$(BuildNumber)"

      # Regex to find and update version numbers in AssemblyInfo or other version files
      $versionRegex = "\d+\.\d+\.\d+"

      # Update the .NET assemblies with the new version number
      Write-Host "Searching for .NET assemblies ..."
      $files = gci $(Build.SourcesDirectory) -Recurse -Include @("*Properties*", "*My Project*") | 
               ?{$_.PSIsContainer} | 
               foreach { gci -Path $_.FullName -Recurse -Include AssemblyInfo.* }

      if ($files.count -gt 0) {
        Write-Host "Applying $version to the following file(s):"
        foreach ($file in $files) {
          $filecontent = Get-Content $file
          attrib $file -r
          $filecontent -replace $versionRegex, $version | Out-File $file
          Write-Host "`t$file"
        }
      } else {
        Write-Host "Found no files."
      }
