- name: Handle Freshworks Incident
  hosts: localhost
  gather_facts: no

  vars:
    basic_auth: "Basic {{ (github_username + ':' + github_password)|b64encode }}"

  tasks:
    - name: Debug template data
      debug:
        msg: "Environment vars: {{ cluster }}"

    - name: Set API URL based on environment
      set_fact:
        api_url: "https://{{ cluster | lower }}fsgateway9-admin.test.com:8001/myapi/rest/gacmd/v1/webusers/{{ username }}"
      when: username is defined

    - name: Check if user exists
      uri:
        url: "{{ api_url }}"
        method: GET
        headers:
          Authorization: "{{ basic_auth }}"
        status_code: [200, 401]
        validate_certs: no
        return_content: yes
      register: user_check_response

    - name: Extract email from XML response if user exists
      when: user_check_response.status == 200
      set_fact:
        user_email: "{{ user_check_response.content | xml | selectattr('tag', '==', 'email') | map(attribute='text') | first }}"

    - name: Send email if user does not exist
      when: user_check_response.status == 401
      set_fact:
        user_email: "{{ user_check_response.content | xml | selectattr('tag', '==', 'email') | map(attribute='text') | first }}"
      block:
        - name: Send notification that account does not exist
          mail:
            to: "{{ user_email }}"
            subject: "Account Does Not Exist"
            body: "The account for user {{ username }} does not exist. Please check the details."

    - name: Set payload for unlock action
      when: user_check_response.status == 200
      set_fact:
        payload: >-
          {{
            {'enabled': true}
            if action == 'unlock'
            else
            {'enabled': false}
          }}

    - name: Perform the action on the user account
      when: user_check_response.status == 200
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
      register: result

    - name: Handle Freshservice response based on action result
      when: user_check_response.status == 200
      block:
        - name: Send success notification to user
          when: result.status == 200
          mail:
            to: "{{ user_email }}"
            subject: "Account Action Success"
            body: "The account for user {{ username }} has been successfully {{ action }}ed."

        - name: Send failure notification to user
          when: result.status != 200
          mail:
            to: "{{ user_email }}"
            subject: "Account Action Failure"
            body: "The account for user {{ username }} could not be {{ action }}ed. Please investigate further."

    # Additional tasks to handle Freshservice ticket updates as needed...
