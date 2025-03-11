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
    description: 'Docker image tag'
    required: true
  docker_username:
    description: 'Docker login username'
    required: true
  docker_password:
    description: 'Docker login password'
    required: true

runs:
  using: "composite"
  steps:
    - name: Login to Docker
      shell: pwsh
      run: |
        docker login --username "${{ inputs.docker_username }}" --password "${{ inputs.docker_password }}" ${{ inputs.docker_repo }}

    - name: Push Docker Image
      shell: pwsh
      run: |
        docker push ${{ inputs.docker_repo }}/${{ inputs.docker_image_name }}:${{ inputs.docker_version_args }}

    - name: Remove Local Docker Image
      shell: pwsh
      run: |
        $app_image_id = docker images -q ${{ inputs.docker_repo }}/${{ inputs.docker_image_name }}:${{ inputs.docker_version_args }}
        if ($app_image_id) {
          docker image rm $app_image_id -f
