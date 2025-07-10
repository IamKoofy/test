name: "Docker Build (Linux Compatible)"
description: "Composite action to build a Docker image for ROSA deployment"

inputs:
  Dockerfile:
    required: true
    type: string
  DockerRepo:
    required: true
    type: string
  DockerImageName:
    required: true
    type: string
  DockerVersionArgs:
    required: false
    type: string
  DockerContext:
    required: false
    default: .
    type: string

runs:
  using: "composite"
  steps:
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v3

    - name: Login to GitHub Container Registry
      uses: docker/login-action@v3
      with:
        registry: ghcr.io
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}

    - name: Build and Push Docker Image
      shell: bash
      run: |
        IMAGE_TAG="${{ inputs.DockerRepo }}/${{ inputs.DockerImageName }}:${{ github.run_number }}"
        echo "Building image: $IMAGE_TAG"

        docker build \
          -f "${{ inputs.Dockerfile }}" \
          -t "$IMAGE_TAG" \
          ${{ inputs.DockerVersionArgs }} \
          "${{ inputs.DockerContext }}"

        echo "Pushing image: $IMAGE_TAG"
        docker push "$IMAGE_TAG"
