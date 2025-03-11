name: 'Deploy to OpenShift'

on:
  workflow_run:
    workflows: ["Adservice Build"]
    types:
      - completed

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

      - name: Deploy to OpenShift
        uses: ./.github/actions/deploy-openshift
        with:
          E1OpenShiftUrl: ${{ steps.vars.outputs.E1OpenShiftUrl }}
          E1OpenShiftToken: ${{ steps.vars.outputs.E1OpenShiftToken }}
          DeployToCert: ${{ steps.vars.outputs.DeployToCert }}
          E2OpenShiftUrl: ${{ steps.vars.outputs.E2OpenShiftUrl }}
          E2OpenShiftToken: ${{ steps.vars.outputs.E2OpenShiftToken }}
          OpenShiftProjectName: ${{ steps.vars.outputs.OpenShiftProjectName }}
          DeploymentConfigName: ${{ steps.vars.outputs.DeploymentConfigName }}
          ContainerName: ${{ steps.vars.outputs.ContainerName }}
          DockerDevRepo: ${{ steps.vars.outputs.DockerDevRepo }}
          DockerCertRepo: ${{ steps.vars.outputs.DockerCertRepo }}
          DockerImageName: "my-docker-image:${{ github.run_number }}"
          CertApprovalEmail: ${{ steps.vars.outputs.CertApprovalEmail }}
