name: Deploy Image to OpenShift

on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Select the deployment environment"
        required: true
        type: string
      docker_image_tag:
        description: "Docker image tag"
        required: true
        type: string

jobs:
  deploy:
    runs-on: windows-latest

    steps:
      - name: Set OpenShift Variables
        shell: powershell
        run: |
          if ("${{ github.event.inputs.environment }}" -eq "Dev") {
            echo "OPENSHIFT_URL=dev.openshift.com" | Out-File -Append -FilePath $Env:GITHUB_ENV
            echo "OPENSHIFT_TOKEN=${{ secrets.OPENSHIFT_DEV_TOKEN }}" | Out-File -Append -FilePath $Env:GITHUB_ENV
            echo "DOCKER_REPO=${{ secrets.DOCKER_DEV_REPO }}" | Out-File -Append -FilePath $Env:GITHUB_ENV
          } elseif ("${{ github.event.inputs.environment }}" -eq "Cert") {
            echo "OPENSHIFT_URL=cert.openshift.com" | Out-File -Append -FilePath $Env:GITHUB_ENV
            echo "OPENSHIFT_TOKEN=${{ secrets.OPENSHIFT_CERT_TOKEN }}" | Out-File -Append -FilePath $Env:GITHUB_ENV
            echo "DOCKER_REPO=${{ secrets.DOCKER_CERT_REPO }}" | Out-File -Append -FilePath $Env:GITHUB_ENV
          }

      - name: Deploy to OpenShift
        shell: powershell
        run: |
          & c:\scripts\deploy-docker-image.ps1 `
            -ocurl $Env:OPENSHIFT_URL `
            -octoken $Env:OPENSHIFT_TOKEN `
            -project "my-project" `
            -dcname "my-deployment-config" `
            -container "my-container" `
            -image "$Env:DOCKER_REPO/${{ github.event.inputs.docker_image_tag }}"
