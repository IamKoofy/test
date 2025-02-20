name: templates_build_task_groups_sonarqube_pre_build_vstesttaskgroup

inputs:
  SonarQubeProjectName:
    required: false
    type: string
  SonarQubeExclusions:
    required: false
    type: string
  SonarQubeServiceConnection:
    required: false
    default: GBT Code Analysis
    type: string
  SonarQubeUrl:  # NEW INPUT for URL
    required: true
    type: string
  SonarQubeToken:  # NEW INPUT for Token
    required: true
    type: string

runs:
  using: composite
  steps:
    - name: Prepare Project Files for SonarQube Scan
      shell: pwsh
      run: |
        function Update-ProjectFile($file) {
          $fileContent = Get-Content -Path $file.FullName
          if ($fileContent -match "<ProjectGuid>") {
            Write-Host "$($file.Name) already has a project id assigned"
            return
          }
          [xml]$xmlFile = $fileContent
          $node = $xmlFile.SelectSingleNode("//Project/PropertyGroup")
          $child = $xmlFile.CreateElement("ProjectGuid")
          $child.InnerText = "{"+[guid]::NewGuid().ToString().ToUpper()+"}"
          $node.AppendChild($child)
          $xmlFile.Save($file.FullName)
          Write-Host "$($file.Name) has been assigned a new project id"
        }
        Get-ChildItem -Path ${{ github.workspace }} -Filter *.csproj -Recurse | ForEach-Object { Update-ProjectFile $_ }

    - name: Install SonarScanner (Temporary)
      shell: pwsh
      run: |
        $sonarScannerPath = "$env:GITHUB_WORKSPACE\sonar-tools"
        New-Item -ItemType Directory -Path $sonarScannerPath -Force
        dotnet tool install --tool-path $sonarScannerPath dotnet-sonarscanner
        echo "SONAR_SCANNER_PATH=$sonarScannerPath\dotnet-sonarscanner.exe" | Out-File -FilePath $env:GITHUB_ENV -Append
        Write-Host "SonarScanner installed temporarily"

    - name: Prepare SonarQube for Scan
      shell: pwsh
      run: |
        Write-Host "Preparing SonarQube for scan..."
        & $env:SONAR_SCANNER_PATH begin `
          /k:"${{ inputs.SonarQubeProjectName }}" `
          /d:sonar.host.url="${{ inputs.SonarQubeUrl }}" `
          /d:sonar.login="${{ inputs.SonarQubeToken }}" `
          /d:sonar.projectVersion="${{ github.run_number }}" `
          /d:sonar.exclusions="${{ inputs.SonarQubeExclusions }}" `
          /d:sonar.test.exclusions="${{ inputs.SonarQubeExclusions }}" `
          /d:sonar.coverage.exclusions="**/*Tests*.cs,**/Models/**/*,**/Data/**/*,**/Program.cs,**/Startup.cs,${{ inputs.SonarQubeExclusions }}"

    - name: Remove SonarScanner After Execution
      if: always()
      shell: pwsh
      run: |
        Remove-Item -Path "$env:GITHUB_WORKSPACE\sonar-tools" -Recurse -Force
        Write-Host "SonarScanner removed after execution"
