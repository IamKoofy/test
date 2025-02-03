---
- name: Login to OpenShift
  shell: >
    {% if sr_environment == 'CDE' %}
    oc login --token={{ cde_api_token }} --server={{ cde_api_url }}
    {% else %}
    oc login --token={{ non_cde_api_token }} --server={{ non_cde_api_url }}
    {% endif %}
  register: oc_login
  failed_when: oc_login.rc != 0

- name: Get initial pod count
  shell: >
    oc get pods -n {{ sr_project }} --selector=deploymentconfig={{ sr_service }} -o jsonpath="{.items[?(@.status.phase=='Running')].metadata.name}" | wc -w
  register: initial_pod_count
  failed_when: initial_pod_count.rc != 0

- name: Restart pods based on condition
  block:
    - name: Restart all pods related to DeploymentConfig
      shell: >
        oc delete pod -n {{ sr_project }} --selector=deploymentconfig={{ sr_service }}
      register: restart_pods_output
      failed_when: restart_pods_output.rc != 0
      when: sr_restart_all | lower | trim == 'yes'

    - name: Restart specific pods
      shell: >
        oc delete pod -n {{ sr_project }} {{ sr_pod_names.split(',') | join(' ') }}
      register: restart_pods_output
      failed_when: restart_pods_output.rc != 0
      when: sr_restart_all | lower | trim != 'yes'

- name: Wait for pods to stabilize
  shell: >
    oc wait --for=condition=Ready pods -n {{ sr_project }} --selector=deploymentconfig={{ sr_service }} --timeout=300s
  register: wait_result
  failed_when: wait_result.rc != 0

- name: Get final pod count
  shell: >
    oc get pods -n {{ sr_project }} --selector=deploymentconfig={{ sr_service }} -o jsonpath="{.items[?(@.status.phase=='Running')].metadata.name}" | wc -w
  register: final_pod_count
  failed_when: final_pod_count.rc != 0

- name: Validate pod count
  fail:
    msg: "Pod count mismatch! Initial: {{ initial_pod_count.stdout }}, Final: {{ final_pod_count.stdout }}"
  when: initial_pod_count.stdout != final_pod_count.stdout

# Freshservice SR Update Tasks
- name: Close Freshservice SR if restart successful
  when: initial_pod_count.stdout == final_pod_count.stdout
  block:
    - name: Reply to the user that pod restart is successful
      uri:
        url: "{{ api_url }}/{{ sr_id }}/reply"
        method: POST
        url_username: KEY
        url_password:
        return_content: yes
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
        headers:
          Content-Type: "application/json"
        body: >-
          {
            "body": "POD restart completed successfully",
            "cc_emails": [ "abhishek.j@amexgbt.com" ]
          }
        status_code: [200, 201, 204]
        validate_certs: no

    - name: Close the service request in Freshservice
      uri:
        url: "{{ api_url }}/{{ sr_id }}"
        method: PUT
        url_username: KEY
        url_password:
        return_content: yes
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
        headers:
          Content-Type: "application/json"
        body: >-
          {
            "ticket": {
              "status": 5,
              "description": "Pod restart completed successfully, closing this SR"
            }
          }
        status_code: [200, 201, 204]
        validate_certs: no

- name: Set Freshservice SR to pending if restart failed
  when: initial_pod_count.stdout != final_pod_count.stdout
  block:
    - name: Reply to the user that pod restart failed
      uri:
        url: "{{ api_url }}/{{ sr_id }}/reply"
        method: POST
        url_username: KEY
        url_password:
        return_content: yes
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
        headers:
          Content-Type: "application/json"
        body: >-
          {
            "body": "Pod restart could not be completed. The internal team is investigating.",
            "cc_emails": [ "abhishek.j@amexgbt.com" ]
          }
        status_code: [200, 201, 204]
        validate_certs: no

    - name: Set ticket status to pending in Freshservice
      uri:
        url: "{{ api_url }}/{{ sr_id }}"
        method: PUT
        url_username: KEY
        url_password:
        return_content: yes
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
        headers:
          Content-Type: "application/json"
        body: >-
          {
            "ticket": {
              "status": 3,
              "description": "Pod restart failed. Internal team is investigating.",
              "custom_fields": {
                "pending_substatus": "Awaiting Internal Team Action",
                "agent_notes": "Pod restart failed. Internal team will investigate further."
              }
            }
          }
        status_code: [200, 201, 204]
        validate_certs: no
