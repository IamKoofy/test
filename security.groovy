- name: Login to OpenShift
  shell: >
    {% if sr_environment == 'CDE' %}
    oc login --token={{ cde_token }} --server={{ cde_api_url }}
    {% else %}
    oc login --token={{ token }} --server={{ non_cde_api_url }}
    {% endif %}
  register: oc_login
  failed_when: oc_login.rc != 0

**- name: Validate if OpenShift project exists**
  shell: oc get project {{ sr_project }} --no-headers
  register: project_check
  **failed_when: false**
  changed_when: false

**- name: Fail and close ticket if project does not exist**
  when: project_check.rc != 0
  block:
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
      meta: end_play**

**- name: Validate if DeploymentConfig exists in the project**
  shell: oc get dc -n {{ sr_project }} {{ sr_service }} --no-headers
  register: dc_check
  **failed_when: false**
  changed_when: false

**- name: Fail and close ticket if DC does not exist**
  when: dc_check.rc != 0
  block:
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
            "body": "Dear user,<br><br>Your request for pod restart could not be processed because the provided <b>deployment config '{{ sr_service }}'</b> does not exist in the project '{{ sr_project }}'. Please check the values and raise a new request.<br><br>Regards,<br>GBT EPaaS Team",
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
              "description": "Pod restart request closed - invalid deployment config name provided."
            }
          }
        status_code: [200, 201, 204]
        validate_certs: no

    - name: End play for invalid deployment config
      meta: end_play**
