name: 'Push Docker Image'
description: 'Logs in to Docker, pushes an image, and removes the local copy'

inputs:
  docker_repo:
    description: 'Docker repository URL'
    required: true
  docker_image_name:
    description: 'Docker image name'
    required: true
  docker_version_args:
    description: 'Docker image tag (optional override)'
    required: false
  docker_username:
    description: 'Docker login username'
    required: true
  docker_password:
    description: 'Docker login password'
    required: true

runs:
  using: "composite"
  steps:
    - name: Docker Login
      shell: pwsh
      run: |
        echo "${{ inputs.docker_password }}" | docker login ${{ inputs.docker_repo }} --username ${{ inputs.docker_username }} --password-stdin

    - name: Push Docker Image
      shell: pwsh
      run: |
        $tag = "${{ inputs.docker_version_args }}"
        if (-not $tag) {
          $tag = "${{ github.run_number }}"
        }
        docker push "${{ inputs.docker_repo }}/${{ inputs.docker_image_name }}:$tag"

    - name: Remove Local Docker Image
      shell: pwsh
      run: |
        $tag = "${{ inputs.docker_version_args }}"
        if (-not $tag) {
          $tag = "${{ github.run_number }}"
        }
        $image = "${{ inputs.docker_repo }}/${{ inputs.docker_image_name }}:$tag"
        $app_image_id = docker images -q $image
        if ($app_image_id) {
          docker image rm $app_image_id -f
        }
        docker context use default

    - name: Remove Dangling Images
      uses: AMEX-GBTG-Sandbox/github-actions-shared-lib/.github/actions/remove-dangling-images@main
