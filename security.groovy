name: Build and Push Image

on:
  workflow_dispatch:
    inputs:
      environment:
        description: "Select the deployment environment"
        required: true
        default: "Dev"
        type: choice
        options:
          - Dev
          - Cert

jobs:
  build-and-push:
    runs-on: windows-latest
    env:
      DOCKER_IMAGE_TAG: my-image:${{ github.run_number }}
    
    steps:
      - name: Trigger Deployment Workflow
        shell: powershell
        run: |
          $jsonPayload = @{
            ref = "main"
            inputs = @{
              environment = "${{ github.event.inputs.environment }}"  # Pass user-selected value
              docker_image_tag = "${{ env.DOCKER_IMAGE_TAG }}"
            }
          } | ConvertTo-Json -Compress -Depth 3

          Invoke-WebRequest -Uri "https://api.github.com/repos/${{ github.repository }}/actions/workflows/release.yml/dispatches" `
            -Method POST `
            -Headers @{
              Authorization = "token ${{ secrets.GITHUB_TOKEN }}"
              Accept = "application/vnd.github.v3+json"
              "Content-Type" = "application/json"
            } `
            -Body $jsonPayload
