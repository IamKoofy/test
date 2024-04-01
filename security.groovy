---
- name: Take OpenShift backups
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
    github_repo_url: "https://github.com/epaas/backip.git"
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

    - name: Push backup location to GitHub repository
      uri:
        url: "{{ github_repo_url }}/non-cde-dev/backup_{{ ansible_date_time.iso8601_basic }}"
        method: PUT
        body: "{{ backup_location }}"
        headers:
          Authorization: "token YOUR_GITHUB_TOKEN"
        status_code: 200
