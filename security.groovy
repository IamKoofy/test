---
- name: Automate Sprint Release
  hosts: localhost
  gather_facts: false

  tasks:
    - name: Get Nexus Credentials from AWX
      community.general.awx_credentials:
        name: "Your Nexus Credential Name"  # Replace with the actual name of your Nexus credential
      register: nexus_credentials

    - name: Download Model Directory ZIP from Nexus with Authentication
      ansible.builtin.uri:
        url: "{{ nexus_base_url }}/{{ nexus_repository_path }}/{{ model_zip_file }}"
        dest: "{{ extraction_folder }}/"
        follow_redirects: all
        user: "{{ nexus_credentials.results[0].inputs.username }}"
        password: "{{ nexus_credentials.results[0].inputs.password }}"
        force_basic_auth: yes
      register: download_result

    # Rest of your tasks




---
- name: Automate Sprint Release
  hosts: localhost
  gather_facts: false

  tasks:
    - name: Get OpenShift Credentials from AWX
      community.general.awx_credentials:
        name: "Your OpenShift Credential Name"  # Replace with the actual name of your OpenShift credential
      register: openshift_credentials

    - name: Extract OpenShift Token
      set_fact:
        openshift_token: "{{ openshift_credentials.results[0].inputs.token }}"

    - name: Login to OpenShift
      ansible.builtin.command: "oc login --server={{ server }} --token={{ openshift_token }}"
