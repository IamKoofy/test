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
runs:
  using: "composite"
  steps:
    - name: Create artifact directory if not exists
      run: mkdir -p $GITHUB_WORKSPACE/artifacts
      shell: bash

    - name: Copy files to artifact directory
      run: cp -r ${{ inputs.path }} $GITHUB_WORKSPACE/artifacts/${{ inputs.name }}
      shell: bash

    - name: Compress artifact
      run: tar -czf $GITHUB_WORKSPACE/artifacts/${{ inputs.name }}.tar.gz -C $GITHUB_WORKSPACE/artifacts ${{ inputs.name }}
      shell: bash

    - name: Upload artifact using GitHub API
      run: |
        ARTIFACT_NAME=${{ inputs.name }}
        ARTIFACT_PATH=$GITHUB_WORKSPACE/artifacts/${{ inputs.name }}.tar.gz
        echo "Uploading artifact $ARTIFACT_NAME..."
        curl -X POST -H "Authorization: Bearer ${{ secrets.GITHUB_TOKEN }}" \
          -H "Accept: application/vnd.github.v3+json" \
          -F "name=$ARTIFACT_NAME" \
          -F "file=@$ARTIFACT_PATH" \
          https://api.github.com/repos/${{ github.repository }}/actions/artifacts
      shell: bash
