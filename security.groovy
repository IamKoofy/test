# pipeline-vars.yml
NUGET_AUTH_TOKEN: your-secret-token
NUGET_FEED_URL: https://your-nuget-feed-url

MAJOR_VERSION: 1
MINOR_VERSION: 0
BUILD_NUMBER: 123
REVISION: 0

SOLUTION_FILE: path/to/solution.sln
PATH_TO_PROJECT: src/YourProject
PATH_TO_TEST_PROJECT: tests/YourTestProject
COMPONENT_NAME: YourComponent
BUILD_CONFIGURATION: Release

SONARQUBE_PROJECT_NAME: YourProject
SONARQUBE_EXCLUSIONS: "**/*.test.cs"
SONARQUBE_SERVICE_CONNECTION: GBT Code Analysis

SNYK_SCAN_ENABLED: true
SNYK_AUTH_TOKEN: your-snyk-token
SNYK_ORG: your-org
SNYK_PROJECT_NAME: your-project






 - name: Load Pipeline Variables
        id: load-vars
        shell: bash
        run: |
          echo "Loading pipeline variables from pipeline-vars.yml"
          while IFS=": " read -r key value; do
            if [[ ! -z "$key" && ! "$key" =~ ^# ]]; then
              echo "$key=${value}" >> $GITHUB_ENV
            fi
          done < pipeline-vars.yml
