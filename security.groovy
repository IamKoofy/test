- name: Push renamed backup folder to Git repository
  git:
    repo: "{{ github_repo_url }}/non-cde-dev/"
    dest: "/tmp/{{ namespace }}_backup_{{ ansible_date_time.iso8601_basic }}"
    force: yes
    push: yes
    remote: origin
    branch: main
    accept_hostkey: yes  # If needed
    username: "{{ git_username }}"
    password: "{{ git_password }}"
  delegate_to: localhost
  become: no  # No privilege escalation needed for pushing to Git
  when: env_var == "non-cde-dev" and oc_login_result.rc == 0
  register: git_push_result
