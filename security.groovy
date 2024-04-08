
- name: Gather backup files
  find:
    paths: "/tmp/{{ namespace }}/non-cde-dev/backup-1"
    recurse: yes
  register: backup_files
  when: env_var == "non-cde-dev" and oc_login_result.rc == 0

- name: Fetch backup files
  fetch:
    src: "/tmp/{{ namespace }}/non-cde-dev/backup-1/{{ item }}"
    dest: "{{ backup_files_dir }}"
    flat: yes
    fail_on_missing: no
  loop: "{{ query('fileglob', '/tmp/{{ namespace }}/non-cde-dev/backup-1/*') }}"
  when: env_var == "non-cde-dev" and oc_login_result.rc == 0
- name: Push backup location to GitHub repository
  uri:
    url: "{{ github_repo_url }}/non-cde-dev/backup_{{ namespace }}_{{ ansible_date_time.iso8601_basic }}"
    method: PUT
    body: "{{ lookup('file', backup_files_dir + '/' + item.path | basename) }}"
    headers:
      Authorization: "Basic {{ github_username }}: {{ github_password }}"
    status_code: 200
    validate_certs: no
    follow_redirects: all
  with_items: "{{ backup_files.files }}"
  when: env_var == "non-cde-dev" and oc_login_result.rc == 0
