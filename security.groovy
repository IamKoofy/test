- name: Push backup folder to Git repository
  git:
    repo: "{{ github_repo_url }}"
    dest: "{{ backup_location }}/non-cde-dev/{{ namespace }}/folder_{{ ansible_date_time.iso8601_basic }}"
    update: yes
    force: yes  # Optional, force push even if remote updates are ahead
    branch: master  # Specify the branch to push to, defaults to 'master'
    key: "{{ lookup('env', 'ANSIBLE_VAULT_PASSWORD_FILE') }}"  # Optional, use a vault password file
  delegate_to: localhost
  become: no  # No privilege escalation needed for pushing to Git
  when: env_var == "non-cde-dev" and oc_login_result.rc == 0
  register: git_push_result




- name: Copy backup files to backup directory
   copy:
    src: "{{ backup_location }}"
    dest: "{{ backup_location }}/non-cde-dev/{{ namespace }}/folder_{{ ansible_date_time.iso8601_basic }}"
   when: env_var == "non-cde-dev" and oc_login_result.rc == 0
