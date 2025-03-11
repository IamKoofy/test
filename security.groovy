name: 'Push Docker Image'
description: 'Pushes a Docker image and cleans up unused images'
inputs:
  docker_repo:
    description: 'Docker Repository'
    required: true
  docker_image_name:
    description: 'Docker Image Name'
    required: true
  docker_version_args:
    description: 'Docker Version/Tag'
    required: true
  docker_username:
    description: 'Docker Username'
    required: true
  docker_password:
    description: 'Docker Password'
    required: true
runs:
  using: "composite"
  steps:
    - name: Login to Docker Registry
      shell: bash
      run: |
        echo "${{ inputs.docker_password }}" | docker login --username "${{ inputs.docker_username }}" --password-stdin ${{ inputs.docker_repo }}

    - name: Push Docker Image
      shell: bash
      run: |
        docker push ${{ inputs.docker_repo }}/${{ inputs.docker_image_name }}:${{ inputs.docker_version_args }}

    - name: Remove Dangling Images
      uses: ./.github/actions/remove-dangling-docker-images

    - name: Remove Local Copy of Docker Image
      shell: bash
      run: |
        app_image_id=$(docker images -q ${{ inputs.docker_repo }}/${{ inputs.docker_image_name }}:${{ inputs.docker_version_args }})
        docker image rm $app_image_id -f
        docker context use default
