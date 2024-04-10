- name: Push renamed backup folder to Git repository
  shell: |
    git clone "{{ git_url }}"
    git config --global user.name "{{ github_username }}"
    git config --global user.email "{{ github_username }}@test.com"
    cd ePaaS-Backup/{{ env_var }}

    # Define the directory structure based on env_var
    if [ "{{ env_var }}" == "non_cde_dev" ]; then
      dir="non_cde/dev/{{ namespace }}"
    elif [ "{{ env_var }}" == "non_cde_qa" ]; then
      dir="non_cde/qa/{{ namespace }}"
    else
      dir=""
    fi

    # Create directory if not exists
    if [ ! -d "$dir" ]; then
      mkdir -p "$dir"
    fi

    # Copy backup folder
    cp -r "/tmp/{{ namespace }}_backup_{{ ansible_date_time.iso8601_basic }}" "$dir"

    # Add, commit, and push changes
    git add .
    git commit -m "Backup commit"
    git push -u origin main
  no_log: true
  args:
    executable: /bin/bash
  delegate_to: localhost
