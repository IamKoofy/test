---
- name: Handle Freshservice Incident
  hosts: localhost
  gather_facts: no

  vars:
    api_endpoints:
      dev: "https://dev.api.example.com:8001/go/rest/gacmd/v1/webusers/"
      qa: "https://qa.api.example.com:8001/go/rest/gacmd/v1/webusers/"
      prod: "https://prod.api.example.com:8001/go/rest/gacmd/v1/webusers/"

  tasks:
    - name: Parse incident details
      set_fact:
        environment: "{{ environment | lower }}"
        action: "{{ action | lower }}"
        username: "{{ username }}"
        incident_ticket_number: "{{ incident_ticket_number }}"

    - name: Validate input
      fail:
        msg: "Invalid environment '{{ environment }}' provided."
      when: api_endpoints[environment] is not defined

    - name: Set API endpoint
      set_fact:
        api_url: "{{ api_endpoints[environment] }}{{ username }}"

    - name: Prepare payload for unlock/lock
      set_fact:
        payload: >
          {{
            {
              "enabled": "true" if action == "unlock" else "false"
            }
          }}

    - name: Get API credentials
      set_fact:
        api_user: "{{ lookup('env', 'API_USER') }}"
        api_pass: "{{ lookup('env', 'API_PASS') }}"

    - name: Encode credentials
      set_fact:
        basic_auth: "{{ api_user }}:{{ api_pass }} | b64encode }}"

    - name: Call API to unlock/lock account
      uri:
        url: "{{ api_url }}"
        method: PUT
        body: "{{ payload | to_json }}"
        headers:
          Authorization: "Basic {{ basic_auth }}"
          Content-Type: "application/json"
        status_code: [200, 201, 204]
      register: api_result

    - name: Handle API response
      debug:
        msg: "User {{ username }} has been {{ action }}ed successfully in {{ environment }} environment. Incident ticket number: {{ incident_ticket_number }}"
