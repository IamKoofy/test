name: "Docker Build"
description: "Composite action to build a Docker image"

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
    required: true
    type: string
  DockerContext:
    required: true
    type: string
  DockerFolder:
    required: false
    type: string
    default: 'C:\DockerShare'

runs:
  using: "composite"
  steps:
    - name: Build Docker Image
      shell: pwsh
      run: |
        docker context use "${{ inputs.DockerContext }}"
        docker build -f "${{ inputs.Dockerfile }}" -t "${{ inputs.DockerRepo }}/${{ inputs.DockerImageName }}:${{ inputs.DockerVersionArgs }}" "${{ inputs.DockerFolder }}"
