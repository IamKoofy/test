
    - name: Parse the incident data
      set_fact:
        incident_ids: "{{ incident_response.json | json_query('tickets[].display_id') }}"

    - name: Loop over incident IDs and gather account information
      loop: "{{ incident_ids }}"
      uri:
        url: "https://test.freshservice.com/api/v2/tickets/{{ item }}/requested_items"
        method: GET
        headers:
          Authorization: "Basic {{ freshservice_token | b64encode }}"
        return_content: yes
        status_code: 200
      register: ticket_data
      delegate_to: localhost

    - name: Set facts for account information
      set_fact:
        accounts_info: "{{ ticket_data.json.requested_items | json_query('[*].{cluster: custom_fields.environment, username: custom_fields.account_to_be_unlocked, action: `unlock`}') }}"

    - name: Trigger AWX job via API if incidents are found
      when: incident_ids | length > 0
      uri:
        url: "{{ awx_api_url }}"
        method: POST
        headers:
          Authorization: "Bearer {{ awx_api_token }}"
          Content-Type: "application/json"
        body_format: json
        body: 
          extra_vars:
            accounts: "{{ accounts_info }}"
        status_code: 201
      delegate_to: localhost
