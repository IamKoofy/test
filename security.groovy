- name: Copy Dockerfile to Docker context
  shell: pwsh
  run: |
    Copy-Item "${{ inputs.Dockerfile }}" "d:/DockerShare/${{ github.run_id }}/Dockerfile"
