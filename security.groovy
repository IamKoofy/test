- name: Rename backup directory
  command: mv "{{ backup_dir }}" "/tmp/{{ namespace }}_backup_{{ ansible_date_time.iso8601_basic }}"
  when: env_var == "non-cde-dev" and oc_login_result.rc == 0

- name: Push renamed backup folder to Git repository
  git:
    repo: "{{ github_repo_url }}/non-cde-dev/"
    dest: "/tmp/{{ namespace }}_backup_{{ ansible_date_time.iso8601_basic }}"
    update: yes
    force: yes  # Optional, force push even if remote updates are ahead
    branch: master  # Specify the branch to push to, defaults to 'master'
  delegate_to: localhost
  become: no  # No privilege escalation needed for pushing to Git
  when: env_var == "non-cde-dev" and oc_login_result.rc == 0
  register: git_push_result
