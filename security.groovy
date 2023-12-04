---
- name: Automate Sprint Release
  hosts: localhost
  gather_facts: false

  tasks:
    - set_fact:
        nexus_base_url: "https://nexus.example.com/repository"
        extraction_folder: "/Users/Model"
        oc_project_name: "project_name"
        oc_project_path: "/app/project_path"
        prod_pod_label_selector: "app=prod-app"
        model_directory: "model_directory"
        model_zip_file: "model_directory.zip"  # Update with the actual zip file name

    - name: Download Model Directory ZIP from Nexus with Authentication
      ansible.builtin.uri:
        url: "{{ nexus_base_url }}/{{ nexus_repository_path }}/{{ model_zip_file }}"
        dest: "{{ extraction_folder }}/"
        follow_redirects: all
        user: "{{ nexus_username }}"
        password: "{{ nexus_password }}"
        force_basic_auth: yes
      register: download_result
      environment:
        NEXUS_USERNAME: "{{ nexus_username }}"
        NEXUS_PASSWORD: "{{ nexus_password }}"  # This should be a secret in AWX

    - name: Fail if download failed
      ansible.builtin.fail:
        msg: "Failed to download the Model Directory ZIP from Nexus"
      when: download_result.status != 200

    - name: Unzip Model Directory
      ansible.builtin.unarchive:
        src: "{{ extraction_folder }}/{{ model_zip_file }}"
        dest: "{{ extraction_folder }}"
        remote_src: false  # Important to specify this when unzipping from the control host

    - name: Login to OpenShift
      ansible.builtin.command: "oc login"
      args:
        cmd: "oc login"

    - name: Switch to the test project
      ansible.builtin.command: "oc project {{ oc_project_name }}"

    - name: Verify the path
      ansible.builtin.command: "pwd"
      args:
        chdir: "{{ extraction_folder }}"

    - name: List the pods
      ansible.builtin.command: "oc get pods --selector={{ prod_pod_label_selector }}"
      register: pods_output

    - name: Copy the model directory to the first prod pod
      ansible.builtin.shell: "oc rsync {{ extraction_folder }}/{{ model_directory }}/{{ model_subdirectory }} {{ pods_output.stdout_lines[1].split()[0] }}:{{ oc_project_path }}"
      when: pods_output.stdout_lines | length > 1
