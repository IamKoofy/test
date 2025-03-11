name: "Deploy to Dev OpenShift"
description: "Deploys a Docker image to Dev (E1) OpenShift"

inputs:
  E1OpenShiftUrl:
    required: true
    description: "OpenShift Dev (E1) API URL"
  E1OpenShiftToken:
    required: true
    description: "OpenShift Dev (E1) Authentication Token"
  OpenShiftProjectName:
    required: true
    description: "OpenShift Project Name"
  DeploymentConfigName:
    required: true
    description: "OpenShift Deployment Config Name"
  ContainerName:
    required: true
    description: "Container Name"
  DockerImageName:
    required: true
    description: "Docker Image Name"

runs:
  using: "composite"
  steps:
    - name: Deploy Image to OpenShift Dev (E1)
      shell: powershell
      run: |
        & "C:\scripts\deploy-docker-image.ps1" `
          -ocurl "${{ inputs.E1OpenShiftUrl }}" `
          -octoken "${{ inputs.E1OpenShiftToken }}" `
          -project "${{ inputs.OpenShiftProjectName }}" `
          -dcname "${{ inputs.DeploymentConfigName }}" `
          -container "${{ inputs.ContainerName }}" `
          -image "${{ inputs.DockerImageName }}"
