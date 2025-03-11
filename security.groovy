name: 'Deploy to OpenShift'

on:
  workflow_dispatch:
    inputs:
      environment:
        description: 'Select Environment to Deploy'
        required: true
        type: choice
        options:
          - Dev
          - Cert

jobs:
  deploy:
    runs-on: windows-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Load Release Variables
        id: vars
        uses: mikefarah/yq@v4
        with:
          cmd: yq eval '. | to_entries | map("::set-output name=\(.key)::\(.value)") | .[]' .github/workflows/vars/release-vars.yml

      - name: Deploy to OpenShift (Dev)
        if: github.event.inputs.environment == 'Dev'
        uses: ./.github/actions/deploy-openshift
        with:
          OpenShiftUrl: ${{ steps.vars.outputs.E1OpenShiftUrl }}
          OpenShiftToken: ${{ steps.vars.outputs.E1OpenShiftToken }}
          OpenShiftProjectName: ${{ steps.vars.outputs.OpenShiftProjectName }}
          DeploymentConfigName: ${{ steps.vars.outputs.DeploymentConfigName }}
          ContainerName: ${{ steps.vars.outputs.ContainerName }}
          DockerRepo: ${{ steps.vars.outputs.DockerDevRepo }}
          DockerImageName: "my-docker-image:${{ github.run_number }}"
          
      - name: Deploy to OpenShift (Cert)
        if: github.event.inputs.environment == 'Cert'
        uses: ./.github/actions/deploy-openshift
        with:
          OpenShiftUrl: ${{ steps.vars.outputs.E2OpenShiftUrl }}
          OpenShiftToken: ${{ steps.vars.outputs.E2OpenShiftToken }}
          OpenShiftProjectName: ${{ steps.vars.outputs.OpenShiftProjectName }}
          DeploymentConfigName: ${{ steps.vars.outputs.DeploymentConfigName }}
          ContainerName: ${{ steps.vars.outputs.ContainerName }}
          DockerRepo: ${{ steps.vars.outputs.DockerCertRepo }}
          DockerImageName: "my-docker-image:${{ github.run_number }}"
