- name: Copy Dockerfile to Docker context
  shell: powershell
  run: |
    $targetFolder = "D:\DockerShare\${{ runner.id }}\${{ github.run_id }}"
    New-Item -ItemType Directory -Path $targetFolder -Force | Out-Null
    Copy-Item "${{ inputs.Dockerfile }}" "$targetFolder\Dockerfile"
