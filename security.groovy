remove-dangling-images/action.yml
name: 'Remove Dangling Docker Images'
description: 'Removes any unwanted dangling images from the Docker system'

runs:
  using: "composite"
  steps:
    - name: Remove Dangling Images
      shell: pwsh
      run: |
        $danglingImages = docker images -f "dangling=true" -q
        if ($danglingImages) {
          $danglingImages | ForEach-Object {
            Write-Host "Removing $_"
            docker rmi $_ -f
          }
        }
