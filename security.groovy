task-groups/version-use-buildnumber.taskgroup.yml:

- name: MajorVersion
  type: string

- name: MinorVersion
  type: string

- name: BuildNumber
  type: string

# Removed the Rev parameter since we no longer need it

steps:

- task: PowerShell@2
  displayName: 'Version Build'
  inputs:
    targetType: 'inline'
    script: |
      $majorVersion="${{ parameters.MajorVersion }}"
      $minorVersion="${{ parameters.MinorVersion }}"
      $buildNumber="${{ parameters.BuildNumber }}"

      Write-Host "Versioning based on the following:-"
      Write-Host "`tMajor Version: $majorVersion, Minor Version: $minorVersion, Build Number: $buildNumber`n"

      # Handle auto generation for BuildNumber if needed
      if ($buildNumber.Trim().ToLower() -eq 'auto') {
        $year = Get-Date -format yy
        $dayNumber = (Get-Date).DayOfYear.ToString().PadLeft(3, '0')
        $buildNumber = "$year$dayNumber"
      }

      # Construct the version as Major.Minor.Build
      $newBuildNumber = "$majorVersion.$minorVersion.$buildNumber"

      # Regex to find and update the version numbers in AssemblyInfo or other version files
      $versionRegex = "\d+\.\d+\.\d+"

      # Update the build number in Azure DevOps
      Write-Host "##vso[build.updatebuildnumber]$newBuildNumber"

      # Update the .NET assemblies with the new version number
      Write-Host "Searching for .NET assemblies ..."
      $files = gci $(Build.SourcesDirectory) -Recurse -Include @("*Properties*", "*My Project*") | 
               ?{$_.PSIsContainer} | 
               foreach { gci -Path $_.FullName -Recurse -Include AssemblyInfo.* }

      if ($files.count -gt 0) {
        Write-Host "Applying $newBuildNumber to the following file(s):"
        foreach ($file in $files) {
          $filecontent = Get-Content $file
          attrib $file -r
          $filecontent -replace $versionRegex, $newBuildNumber | Out-File $file
          Write-Host "`t$file"
        }
      } else {
        Write-Host "Found no files."
      }
