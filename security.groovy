- name: Automate copy model folder to PV each sprint release
  hosts: localhost
  gather_facts: false

  vars:
    nexus_base_url: "{{ nexus_base_url | default('https://repos.nexus.com/repository') }}"
    nexus_repository_path: "{{ nexus_repository_path | default('models') }}"
    model_zip_file: "{{ model_zip_file | default('model_20221128-170018.zip') }}"
    extraction_folder: "{{ extraction_folder | default('/Users/Model') }}"
    oc_project_name: "{{ oc_project_name | default('hotel') }}"
    oc_project_path: "{{ oc_project_path | default('/app/projects/Hotel/') }}"

  tasks:

- name: Copy the model directory to a pod containing "rasaniu"
  ansible.builtin.shell: >
    oc rsync {{ extraction_folder }}/{{ model_directory }}/{{ model_subdirectory }}
    {{ (pods_output.stdout_lines | select('match', 'rasaniu') | first).split()[0] }}:{{ oc_project_path }}
  when: pods_output.stdout_lines | length > 0 and "'rasaniu' in item"


    - name: Download Model Directory ZIP from Nexus with Authentication
      ansible.builtin.uri:
        url: "{{ nexus_base_url }}/{{ nexus_repository_path }}/{{ model_zip_file }}"
        dest: "{{ extraction_folder }}/"
        follow_redirects: all
        user: "{{ nexus_username }}"
        password: "{{ nexus_password }}"
        force_basic_auth: yes
      register: download_result

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
      ansible.builtin.command: "oc login --server={{ server }} --token={{ openshift_token }}"
      environment:
        KUBECONFIG: "{{ extraction_folder }}/kubeconfig"  # Update the kubeconfig path

    - name: Switch to the test project
      ansible.builtin.command: "oc project {{ oc_project_name }}"

    - name: Verify the path
      ansible.builtin.command: "pwd"
      args:
        chdir: "{{ extraction_folder }}"

    - name: List the pods
      ansible.builtin.command: "oc get pods"
      register: pods_output

    - name: Copy the model directory to the first prod pod
      ansible.builtin.shell: "oc rsync {{ extraction_folder }}/{{ model_directory }}/{{ model_subdirectory }} {{ pods_output.stdout_lines[1].split()[0] }}:{{ oc_project_path }}"
      when: pods_output.stdout_lines | length > 1
