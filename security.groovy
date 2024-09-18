---
- name: Handle Freshworks Incident
  hosts: localhost
  gather_facts: no
  tasks:

    - name: Get incidents from filter view
      uri:
        url: "https://test.freshservice.com/helpdesk/tickets/view/100000207639?format=json"
        method: GET
        url_username: qdIwrrVoGVjaN4HCpgC
        url_password: my_password
        return_content: yes
        status_code: 200
        validate_certs: no
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
      register: incidents_response

    - name: Output response
      debug:
        msg: "incidents_response: {{ incidents_response }}"

    - name: Parse incident IDs from response
      set_fact:
        incident_ids: "{{ incidents_response.json | map(attribute='display_id') | list }}"
      when: incidents_response.json | length > 0

    - name: Process the first incident only  # Added: Capture the first ticket's ID
      set_fact:
        first_incident_id: "{{ incident_ids[0] }}"
      when: incident_ids | length > 0

    - name: Get details for the first incident  # Modified: Use first_incident_id instead of looping through all tickets
      uri:
        url: "https://test.freshservice.com/api/v2/tickets/{{ first_incident_id }}/requested_items"
        method: GET
        url_username: qdIwrrVoGVjaN4HCpgC
        url_password: my_password
        return_content: yes
        status_code: 200
        validate_certs: no
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
      register: ticket_data

    - name: Parse the JSON content from ticket_data  # No change here
      set_fact:
        json_data: "{{ ticket_data.results[0].content | from_yaml }}"

    - name: Extract fields from the JSON data  # No change here
      set_fact:
        extracted_info: >-
          {{ json_data.requested_items[0].custom_fields | dict2items
          | selectattr('key', 'in', ['environment', 'platform', 'account_name_to_be_unlocked'])
          | items2dict }}

    - name: Print extracted information  # No change here
      debug:
        var: extracted_info

    - name: Launch AWX job template using API with Basic Auth  # Modified: Use first_incident_id and extracted info for this specific ticket
      uri:
        url: "https://awx.test.com/api/v2/job_templates/562/launch/"
        method: POST
        headers:
          Content-Type: "application/json"
        body_format: json
        body:
          extra_vars:
            cluster: "{{ extracted_info.environment }}"
            platform: "{{ extracted_info.platform }}"
            username: "{{ extracted_info.account_name_to_be_unlocked }}"
            action: "unlock"
            sr_id: "{{ first_incident_id }}"  # Changed: Using first_incident_id instead of looping through all
        status_code: 201
        validate_certs: no
        url_username: "testMFT"
        url_password: "test123"
        force_basic_auth: yes
      register: awx_job_result

    - debug:
        msg: "AWX job result: {{ awx_job_result }}"
