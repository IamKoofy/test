- name: Push renamed backup folder to Git repository
  shell: |
    cd "/tmp/{{ namespace }}_backup_{{ ansible_date_time.iso8601_basic }}"
    git init
    git add .
    git commit -m "Backup commit"
    git remote add origin "{{ github_repo_url }}/non-cde-dev/"
    git push -u origin master
  args:
    executable: /bin/bash
  delegate_to: localhost
