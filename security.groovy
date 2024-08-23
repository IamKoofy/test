---
- name: Handle Freshworks Incident
  hosts: localhost
  gather_facts: no
  vars:
    basic_auth: "Basic {{ (github_username + ':' + github_password) | b64encode }}"

  tasks:
    - name: Debug template data
      debug:
        msg: "Environment vars: {{ cluster }}"

    - name: Set API URL for QA environment
      set_fact:
        api_url: "https://test.com:8001/goanywhere/rest/gacmd/v1/webusers/{{ username }}"
      when: cluster == 'QA' and username is defined

    - name: Set API URL for PROD environment
      set_fact:
        api_url: "https://test.com:8001//goanywhere/rest/gacmd/v1/webusers/{{ username }}"
      when: cluster == 'PROD'

    - name: Set API URL for DEV environment (default)
      set_fact:
        api_url: "https://test.com:8001//goanywhere/rest/gacmd/v1/webusers//{{ username }}"
      when: cluster == 'DEV'

    - name: Debug API URL to verify environment setting
      debug:
        msg: "API URL is set to {{ api_url }}"

    - name: Set payload for unlock action
      set_fact:
        payload: >-
          {{
            {'enabled': true}
            if action == 'unlock'
            else
            {'enabled': false}
          }}

    - name: Perform the action on the user account
      uri:
        url: "{{ api_url }}"
        method: PUT
        headers:
          Content-Type: "application/json"
          Authorization: "{{ basic_auth }}"
        body: "{{ payload | to_json }}"
        status_code: [200, 201, 204]
        validate_certs: no
        follow_redirects: all
        delegate_to: localhost
      register: result

    - name: Debug account action result
      debug:
        var: result

    # Check for success and perform Freshservice API calls
    - name: Handle Freshservice response for successful unlock
      when: result.status == 200
      block:
        - name: Reply to the user that account is unlocked
          uri:
            url: "https://freshservice.test.com/api/v2/tickets/{{ ticket_id }}/reply"
            method: POST
            headers:
              Content-Type: "application/json"
              Authorization: "Basic {{ freshservice_auth_token }}"
            body: >-
              {
                "body": "Account unlocked",
                "cc_emails": "test.email.com"
              }
            status_code: [200]
            validate_certs: no

        - name: Close the service request in Freshservice
          uri:
            url: "https://freshservice.test.com/api/v2/tickets/{{ ticket_id }}"
            method: PUT
            headers:
              Content-Type: "application/json"
              Authorization: "Basic {{ freshservice_auth_token }}"
            body: >-
              {
                "status": 5
              }
            status_code: [200]
            validate_certs: no

    - name: Handle Freshservice response for failed unlock
      when: result.status != 200
      block:
        - name: Set ticket status to pending in Freshservice
          uri:
            url: "https://freshservice.test.com/api/v2/tickets/{{ ticket_id }}"
            method: PUT
            headers:
              Content-Type: "application/json"
              Authorization: "Basic {{ freshservice_auth_token }}"
            body: >-
              {
                "status": 3
              }
            status_code: [200]
            validate_certs: no
