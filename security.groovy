- name: Check if DeploymentConfig exists
  shell: oc get dc -n {{ sr_project }} {{ sr_service }} --no-headers
  register: dc_check
  failed_when: false
  changed_when: false

- block:
    - name: Reply to user - Invalid DeploymentConfig
      uri:
        url: "{{ api_url }}/{{ sr_id }}/reply"
        method: POST
        url_username: TOKEN
        url_password:
        return_content: yes
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
        headers:
          Content-Type: "application/json"
        body: |
          {
            "body": "Dear user,<br><br>Your request for pod restart could not be processed because the provided <b>DeploymentConfig '{{ sr_service }}'</b> does not exist in the project '{{ sr_project }}'.<br><br>Please verify the service name and submit a new request.<br><br>Regards,<br>GBT EPaaS Team",
            "cc_emails": [ "myteam@myteam.com" ]
          }
        status_code: [200, 201, 204]
        validate_certs: no

    - name: Close the SR - invalid DC
      uri:
        url: "{{ api_url }}/{{ sr_id }}"
        method: PUT
        url_username: TOKEN
        url_password:
        return_content: yes
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
        headers:
          Content-Type: "application/json"
        body: >
          {
            "ticket": {
              "status": 5,
              "description": "Pod restart request closed - DeploymentConfig '{{ sr_service }}' not found in project '{{ sr_project }}'."
            }
          }
        status_code: [200, 201, 204]
        validate_certs: no

    - name: End play for invalid deployment config
      meta: end_play
  when: dc_check.rc != 0
