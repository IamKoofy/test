# .github/workflows/release.yml
name: SBP Admin Web Release to Dev

on:
  workflow_run:
    workflows: ["SBP Admin Web Build"]
    types:
      - completed

jobs:
  deploy-dev:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest
    env:
      OpenshiftProjectName: sbp-dev
      DeploymentConfigName: sbp-admin-web
      ContainerName: sbp-admin-web
      DockerImageName: sbp-admin-web
      DockerDevRepo: registry.dev.mycorp.com/sbp

    steps:
    - name: Set image tag from build run
      run: |
        echo "IMAGE_TAG=${{ github.event.workflow_run.head_branch }}-${{ github.event.workflow_run.run_number }}" >> $GITHUB_ENV

    - name: Log in to OpenShift (Dev)
      run: |
        oc login ${{ secrets.ROSA_DEV_URL }} --token=${{ secrets.ROSA_DEV_TOKEN }}
        oc project ${{ env.OpenshiftProjectName }}

    - name: Patch DeploymentConfig with new image
      run: |
        oc patch dc/${{ env.DeploymentConfigName }} --patch \
          "{\"spec\": {\"template\": {\"spec\": {\"containers\": [{\"name\": \"${{ env.ContainerName }}\",\"image\": \"${{ env.DockerDevRepo }}/${{ env.DockerImageName }}:${{ env.IMAGE_TAG }}\"}]}}}}"
