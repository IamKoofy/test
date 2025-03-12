name: Build and Push Docker Image

on:
  workflow_dispatch:
    inputs:
      docker_image_name:
        description: "Docker Image Name"
        required: true
        default: "my-app"

jobs:
  build:
    runs-on: windows-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Log in to Docker Registry
        shell: powershell
        run: |
          echo "${{ secrets.DOCKER_REGISTRY_PASSWORD }}" | docker login ${{ secrets.DOCKER_REGISTRY_URL }} -u "${{ secrets.DOCKER_REGISTRY_USERNAME }}" --password-stdin

      - name: Build Docker Image
        shell: powershell
        run: |
          $tag = "${{ secrets.DOCKER_REGISTRY_URL }}/${{ github.event.inputs.docker_image_name }}:${{ github.run_number }}"
          docker build -t $tag .
          echo "DOCKER_IMAGE_TAG=$tag" | Out-File -FilePath $env:GITHUB_ENV -Encoding utf8

      - name: Push Docker Image
        shell: powershell
        run: |
          docker push "${{ env.DOCKER_IMAGE_TAG }}"

      - name: Trigger Deployment Workflow
        shell: powershell
        run: |
          Invoke-WebRequest -Uri "https://api.github.com/repos/${{ github.repository }}/actions/workflows/deploy.yml/dispatches" `
            -Method POST `
            -Headers @{Authorization = "token ${{ secrets.GITHUB_TOKEN }}"; Accept = "application/vnd.github.v3+json"} `
            -Body (@{ref="main"; inputs=@{docker_image_tag="${{ env.DOCKER_IMAGE_TAG }}"}} | ConvertTo-Json -Compress)

