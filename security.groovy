- name: Run Yarn Build
  shell: pwsh
  run: |
    $env:PATH="C:\Program Files\Volta;$env:PATH"
    yarn run build
