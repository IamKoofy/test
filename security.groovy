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

    - name: Accumulate SR details into a list
      set_fact:
        all_srs_details: "{{ all_srs_details | default([]) + [item.json] }}"
      loop: "{{ sr_details.results }}"
      loop_control:
        loop_var: item  # Use 'item' as the loop variable
      register: accumulated_srs  # Store all accumulated data

    - name: Extract fields from the JSON data
      set_fact:
        extracted_info: >-
          {{ item.requested_items[0].custom_fields
             | dict2items
             | selectattr('key', 'in', ['environment', 'project', 'service'])
             | items2dict }}
      loop: "{{ accumulated_srs.results }}"  # Loop through all accumulated SR details
      loop_control:
        loop_var: item
      register: extracted_info_list  # Store the extracted fields for each SR

    - name: Print extracted SR details
      debug:
        msg: "Extracted details: {{ item }}"
      loop: "{{ extracted_info_list }}"  # Loop through the extracted info for all SRs
      loop_control:
        loop_var: item

    - name: Restart Pods for each SR
      ansible.builtin.include_role:
        name: restart_pods
      vars:
        sr_environment: "{{ item.environment }}"
        sr_project: "{{ item.project }}"
        sr_service: "{{ item.service }}"
        sr_restart_all: "{{ item.custom_fields.restart_all_pods }}"
        sr_pod_names: "{{ item.custom_fields.pod_names }}"
      loop: "{{ extracted_info_list }}"  # Loop through the extracted fields for all SRs
      loop_control:
        loop_var: item
