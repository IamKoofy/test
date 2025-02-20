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
      shell: powershell
      run: |
        function update-project-file($file) {
          $fileContent = Get-Content -path $file.FullName
          if ($fileContent -like ("*ProjectGuid*")) {
            Write-Host "$($file.Name) already has a project id assigned"
            return
          }
          [xml] $xmlFile = $fileContent
          $node = $xmlFile.SelectSingleNode("//Project/PropertyGroup")
          $child = $xmlFile.CreateElement("ProjectGuid")
          $child.InnerText = "{"+[guid]::NewGuid().ToString().ToUpper()+"}"
          $node.AppendChild($child)
          $xmlFile.Save($file.FullName) | Out-Null
          Write-Host "$($file.Name) has been assigned a new project id"
        }
        gci -path ${{ github.workspace }} -Filter *.csproj -Recurse | % { update-project-file $_ }

    - name: Install SonarScanner (Temporary)
      shell: bash
      run: |
        mkdir -p sonar-tools
        dotnet tool install --tool-path sonar-tools dotnet-sonarscanner
        echo "SONAR_SCANNER_PATH=$(pwd)/sonar-tools/dotnet-sonarscanner" >> $GITHUB_ENV
        echo "SonarScanner installed temporarily"

    - name: Prepare SonarQube for Scan
      shell: bash
      run: |
        echo "Preparing SonarQube for scan..."
        $SONAR_SCANNER_PATH begin \
          /k:"${{ inputs.SonarQubeProjectName }}" \
          /d:sonar.host.url="${{ inputs.SonarQubeUrl }}" \
          /d:sonar.login="${{ inputs.SonarQubeToken }}" \
          /d:sonar.projectVersion="${{ github.run_number }}" \
          /d:sonar.exclusions="${{ inputs.SonarQubeExclusions }}" \
          /d:sonar.test.exclusions="${{ inputs.SonarQubeExclusions }}" \
          /d:sonar.coverage.exclusions="**/*Tests*.cs,**/Models/**/*,**/Data/**/*,**/Program.cs,**/Startup.cs,${{ inputs.SonarQubeExclusions }}"

    - name: Remove SonarScanner After Execution
      if: always()
      shell: bash
      run: rm -rf sonar-tools
