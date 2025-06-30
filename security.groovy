- name: Check if project exists
  shell: oc get project {{ sr_project }} --no-headers
  register: project_check
  failed_when: false
  changed_when: false

- block:
    - name: Reply to user - Invalid project
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
            "body": "Dear user,<br><br>Your request for pod restart could not be processed because the provided OpenShift <b>project name '{{ sr_project }}'</b> is incorrect or does not exist. Please check the project name and raise a new request.<br><br>Regards,<br>GBT EPaaS Team",
            "cc_emails": [ "myteam@myteam.com" ]
          }
        status_code: [200, 201, 204]
        validate_certs: no

    - name: Close the SR - invalid project
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
              "description": "Pod restart request closed - invalid project name provided."
            }
          }
        status_code: [200, 201, 204]
        validate_certs: no

    - name: End play for invalid project
      meta: end_play
  when: project_check.rc != 0
