name: templates_build_task_groups_snyk_dotnet_scantaskgroup

inputs:
  SnykScanEnabled:
    required: false
    default: true
    type: boolean
  SnykSolutionFile:
    required: false
    type: string
  SnykAuthToken:
    required: false
    type: string
  SnykOrg:
    required: false
    type: string
  SnykProjectName:
    required: false
    type: string

runs:
  using: composite
  steps:
    - name: Authenticate with Snyk
      if: success() && inputs.SnykScanEnabled == 'true'
      continue-on-error: true
      shell: powershell
      run: d:\snyk\snyk.exe auth ${{ inputs.SnykAuthToken }}

    - name: Run Snyk Code Scan
      if: success() && inputs.SnykScanEnabled == 'true'
      continue-on-error: true
      shell: powershell
      run: |
        Write-Host "Running Snyk scan..."
        $buildNumber = $env:BUILD_BUILDNUMBER.Replace('.', '_')
        d:\snyk\snyk.exe code test --org=${{ inputs.SnykOrg }} --sarif-file-output=results.sarif --file=${{ inputs.SnykSolutionFile }} --severity-threshold=medium --insecure --debug
        d:\snyk\snyk-to-html.exe -o ${{ runner.temp }}/results-code.html -i results.sarif
        d:\snyk\snyk.exe code test --org=${{ inputs.SnykOrg }} --report --project-name=${{ inputs.SnykProjectName }} --severity-threshold=medium --insecure --debug

    - name: Publish Snyk Scan Report
      if: always() # Ensures it runs even if the scan fails
      uses: actions/upload-artifact@v3
      with:
        name: snyk-code-scan-results
        path: ${{ runner.temp }}/results-code.html
