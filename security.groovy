---
- name: Take OpenShift backups and push to Git repo
  hosts: localhost
  vars:
    env_var: "{{ survey_env_var }}"
    namespace: "{{ survey_namespace }}"
    backup_number: "{{ survey_backup_number }}"
    backup_location: "{{ survey_backup_location }}"
    oc_login_urls:
      non-cde-dev: "https://oc-non-cde-dev.example.com"
      cde-dev: "https://oc-cde-dev.example.com"
      # Add more environments and their URLs as needed
    git_repo_url: "https://github.com/yourusername/your-repo.git"
    git_repo_local_path: "/path/to/local/clone"
  tasks:
    - name: Set OpenShift login URL based on environment
      set_fact:
        oc_login_url: "{{ oc_login_urls[env_var] }}"
      when: oc_login_urls[env_var] is defined

    - name: Login to OpenShift
      command: >
        oc login {{ oc_login_url }} -u {{ survey_oc_username }} -p {{ survey_oc_password }}
        creates={{ lookup('env','HOME') + '/.kube/cache/openshift-token' }}
      environment:
        KUBECONFIG: "{{ lookup('env','HOME') + '/.kube/config' }}"
      register: oc_login_result
      ignore_errors: yes
      changed_when: oc_login_result.rc != 0

    - name: Include tasks based on environment
      include_tasks: "{{ env_var }}.yml"
      when: oc_login_result.rc == 0

    - name: Create backup folder in local Git repository
      file:
        path: "{{ git_repo_local_path }}/backups/{{ namespace }}"
        state: directory
      when: oc_login_result.rc == 0

    - name: Copy backup files to local Git repository
      copy:
        src: "{{ backup_location }}"
        dest: "{{ git_repo_local_path }}/backups/{{ namespace }}"
      when: oc_login_result.rc == 0

    - name: Add changes to Git repository
      command: >
        git add .
      args:
        chdir: "{{ git_repo_local_path }}"
      when: oc_login_result.rc == 0

    - name: Commit changes to Git repository
      command: >
        git commit -m "Add OpenShift backups"
      args:
        chdir: "{{ git_repo_local_path }}"
      when: oc_login_result.rc == 0

    - name: Push changes to Git repository
      command: >
        git push origin master
      args:
        chdir: "{{ git_repo_local_path }}"
      when: oc_login_result.rc == 0
