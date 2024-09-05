- task: PowerShell@2
  inputs:
    targetType: 'inline'
    script: |
      $majorVersion = "${{ parameters.MajorVersion }}"
      $minorVersion = "${{ parameters.MinorVersion }}"
      $buildNumber = "${{ parameters.BuildNumber }}"
      .\nbgv cloud --version "$majorVersion.$minorVersion.$buildNumber"
  workingDirectory: 'D:\BuildAgents\1\_work\_temp\data-test'
  displayName: Set Version
