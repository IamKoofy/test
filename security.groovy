- name: Unlock or lock user accounts via GoAPI
  hosts: localhost
  gather_facts: no
  vars:
    api_user: "{{ lookup('env', 'API_USER') }}"
    api_password: "{{ lookup('env', 'API_PASSWORD') }}"
  tasks:
    - name: Set API URL based on environment
      set_fact:
        api_url: >-
          {{
            'https://aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/' + username
            if environment == 'QA'
            else
            'https://prod.aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/' + username
            if environment == 'PROD'
            else
            'https://dev.aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/' + username
          }}

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
        user: "{{ api_user }}"
        password: "{{ api_password }}"
        body: "{{ payload | to_json }}"
        headers:
          Content-Type: "application/json"
        status_code: 200
      delegate_to: localhost
      register: result

    - debug:
        var: result
