---
- name: Fetch SR Details and Validate
  hosts: localhost
  gather_facts: no
  tasks:
    - name: Process each SR
      with_items: "{{ sr_ids }}"
      loop_control:
        loop_var: sr_id
      tasks:
        - name: Get details for each incident
          uri:
            url: "https://amexgbt.freshservice.com/api/v2/tickets/{{ sr_id }}/requested_items"
            method: GET
            url_username: qdIwrrVoGVjaN4HCpgC
            url_password: <your_password_here>
            return_content: yes
            status_code: 200
            validate_certs: no
            body_format: json
            force_basic_auth: yes
            follow_redirects: all
          register: sr_details

        - name: Print results
          debug:
            msg: "Results: {{ sr_details }}"

        - name: Parse the JSON content from ticket data
          set_fact:
            json_data: "{{ sr_details.content | from_json }}" # Updated from 'from_yaml' to 'from_json'

        - name: Extract fields from the JSON data
          set_fact:
            extracted_info: >-
              {{ json_data.requested_items[0].custom_fields
                 | dict2items
                 | selectattr('key', 'in', ['environment', 'project', 'service'])
                 | items2dict }}

        - name: Print results
          debug:
            var: extracted_info

        - name: Trigger Pod Restart for SR
          ansible.builtin.include_role:
            name: restart_pods
          vars:
            sr_environment: "{{ extracted_info.environment }}"
            sr_project: "{{ extracted_info.project }}"
            sr_service: "{{ extracted_info.service }}"
            sr_restart_all: "{{ extracted_info.custom_fields.restart_all_pods }}"
            sr_pod_names: "{{ extracted_info.custom_fields.pod_names }}"
