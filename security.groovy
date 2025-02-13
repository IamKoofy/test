name: Snyk .NET Scan

on:
  workflow_call:
    inputs:
      SnykScanEnabled:
        required: false
        type: boolean
        default: true
      SnykSolutionFile:
        required: true
        type: string
      SnykOrg:
        required: true
        type: string
      SnykProjectName:
        required: true
        type: string
    secrets:
      SNYK_AUTH_TOKEN:
        required: true

jobs:
  snyk-scan:
    if: ${{ inputs.SnykScanEnabled }}
    runs-on: self-hosted  # Inherit the runner from the main pipeline

    steps:
      - name: Authenticate with Snyk
        shell: pwsh
        run: |
          d:\snyk\snyk.exe auth ${{ secrets.SNYK_AUTH_TOKEN }}

      - name: Run Snyk Code Scan
        shell: pwsh
        run: |
          Write-Host "Build Number: $env:GITHUB_RUN_NUMBER"
          $buildNumber = $env:GITHUB_RUN_NUMBER
          $buildNumber = $buildNumber.Replace('.', '_')

          d:\snyk\snyk.exe code test --org=${{ inputs.SnykOrg }} `
            --sarif-file-output=results.sarif `
            --file=${{ inputs.SnykSolutionFile }} `
            --severity-threshold=medium --insecure --debug

          d:\snyk\snyk-to-html.exe -o ${{ github.workspace }}/results-code.html -i results.sarif

          d:\snyk\snyk.exe code test --org=${{ inputs.SnykOrg }} `
            --report --project-name=${{ inputs.SnykProjectName }} `
            --severity-threshold=medium --insecure --debug

      - name: Upload Code Scan Results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: snyk-code-scan-results
          path: results-code.html
