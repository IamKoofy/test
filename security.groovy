name: "Upload Artifact (Internal)"
description: "Uploads an artifact to GitHub Actions without using external actions"
inputs:
  name:
    description: "The name of the artifact"
    required: true
  path:
    description: "The path of the files/folder to be uploaded"
    required: true
  retention-days:
    description: "The number of days to retain the artifact"
    required: false
    default: "30"
  token:
    required: true
runs:
  using: "composite"
  steps:
    - name: Debug GitHub Workspace
      run: echo "GITHUB_WORKSPACE is $env:GITHUB_WORKSPACE"
      shell: powershell

    - name: Create artifact directory if not exists
      run: |
        $artifactDir = "$env:GITHUB_WORKSPACE\artifacts"
        if (!(Test-Path $artifactDir)) {
          New-Item -ItemType Directory -Path $artifactDir | Out-Null
        }
      shell: powershell

    - name: Copy files to artifact directory
      run: Copy-Item -Path "${{ inputs.path }}" -Destination "$env:GITHUB_WORKSPACE\artifacts\${{ inputs.name }}" -Recurse -Force
      shell: powershell

    - name: Compress artifact
      run: Compress-Archive -Path "$env:GITHUB_WORKSPACE\artifacts\${{ inputs.name }}" -DestinationPath "$env:GITHUB_WORKSPACE\artifacts\${{ inputs.name }}.zip" -Force
      shell: powershell

    - name: Upload artifact using GitHub API
      run: |
        ARTIFACT_NAME=${{ inputs.name }}
        ARTIFACT_PATH="$GITHUB_WORKSPACE/artifacts/${{ inputs.name }}.zip"
        echo "Uploading artifact $ARTIFACT_NAME..."
        curl -X POST -H "Authorization: Bearer ${{ inputs.token }}" \
        -H "Accept: application/vnd.github.v3+json" \
        -F "name=$ARTIFACT_NAME" \
        -F "file=@$ARTIFACT_PATH" \
        https://api.github.com/repos/${{ github.repository }}/actions/artifacts
      shell: bash
