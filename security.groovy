name: Display Pipeline Info

on:
  workflow_call:

jobs:
  show-checkout-folder:
    runs-on: self-hosted
    steps:
      - name: Show Checkout Folder
        shell: pwsh
        run: |
          Write-Host "Checkout Folder: $env:GITHUB_WORKSPACE"
