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

    - name: Install SonarScanner for .NET
      shell: bash
      run: |
        dotnet tool install --global dotnet-sonarscanner
        echo "SonarScanner installed"

    - name: Prepare SonarQube for Scan
      shell: bash
      run: |
        echo "Preparing SonarQube for scan..."
        dotnet-sonarscanner begin \
          /k:"${{ inputs.SonarQubeProjectName }}" \
          /d:sonar.host.url="$SONARQUBE_URL" \
          /d:sonar.login="$SONARQUBE_TOKEN" \
          /d:sonar.projectVersion="${{ github.run_number }}" \
          /d:sonar.exclusions="${{ inputs.SonarQubeExclusions }}" \
          /d:sonar.test.exclusions="${{ inputs.SonarQubeExclusions }}" \
          /d:sonar.coverage.exclusions="**/*Tests*.cs,**/Models/**/*,**/Data/**/*,**/Program.cs,**/Startup.cs,${{ inputs.SonarQubeExclusions }}"
      env:
        SONARQUBE_URL: ${{ secrets.SONARQUBE_URL }}
        SONARQUBE_TOKEN: ${{ secrets.SONARQUBE_TOKEN }}
