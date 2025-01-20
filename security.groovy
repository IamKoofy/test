- name: Fetch SR Details and Restart Pods
  hosts: localhost
  gather_facts: no
  vars:
    api_url: "https://amexgbt.freshservice.com/api/v2/tickets"
    auth_username: "qdIwrrVoGVjaN4HCpgC"
    auth_password: "<your_password_here>"
  tasks:
    - name: Get details for each SR
      uri:
        url: "{{ api_url }}/{{ item }}/requested_items"
        method: GET
        url_username: "{{ auth_username }}"
        url_password: "{{ auth_password }}"
        return_content: yes
        status_code: 200
        validate_certs: no
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
      loop: "{{ sr_ids }}"  # Loop through each SR ID passed as extra vars
      loop_control:
        loop_var: sr_id
      register: sr_details
      failed_when: sr_details.status != 200

    - name: Parse the JSON content from ticket data
      set_fact:
        json_data: "{{ sr_details.json }}"
      loop: "{{ sr_ids }}"  # Loop through each SR ID again
      loop_control:
        loop_var: sr_id

    - name: Extract fields from the JSON data
      set_fact:
        extracted_info: >-
          {{ json_data.requested_items[0].custom_fields
             | dict2items
             | selectattr('key', 'in', ['environment', 'project', 'service'])
             | items2dict }}
      loop: "{{ sr_ids }}"  # Loop through each SR ID again
      loop_control:
        loop_var: sr_id

    - name: Print extracted SR details
      debug:
        msg: "Extracted details: {{ extracted_info }}"
      loop: "{{ sr_ids }}"  # Loop through each SR ID again
      loop_control:
        loop_var: sr_id

    - name: Restart Pods for each SR
      ansible.builtin.include_role:
        name: restart_pods
      vars:
        sr_environment: "{{ extracted_info.environment }}"
        sr_project: "{{ extracted_info.project }}"
        sr_service: "{{ extracted_info.service }}"
        sr_restart_all: "{{ extracted_info.custom_fields.restart_all_pods }}"
        sr_pod_names: "{{ extracted_info.custom_fields.pod_names }}"
      loop: "{{ sr_ids }}"  # Loop through each SR ID again
      loop_control:
        loop_var: sr_id
