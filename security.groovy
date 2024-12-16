---
- name: Poll Freshservice for SRs
  hosts: localhost
  gather_facts: no
  tasks:
    - name: Fetch SRs from Freshservice
      uri:
        url: "https://my.freshservice.com/api/v2/tickets/filter?query=(group_id:17000359599 AND tag:'MFT-Automation' AND status:(2 OR 3 OR 6))"
        method: GET
        user: "{{ freshservice_api_user }}"
        password: "{{ freshservice_api_password }}"
        headers:
          Content-Type: "application/json"
        validate_certs: no
      register: sr_response

    - name: Extract SR IDs
      set_fact:
        sr_ids: "{{ sr_response.json.tickets | map(attribute='id') | list }}"
      when: sr_response.json.tickets | length > 0

    - name: Output SR IDs
      debug:
        msg: "Found SRs: {{ sr_ids }}"







---
- name: Fetch SR Details and Validate
  hosts: localhost
  gather_facts: no
  tasks:
    - name: Process each SR
      with_items: "{{ sr_ids }}"
      loop_control:
        loop_var: sr_id
      tasks:
        - name: Fetch SR Details
          uri:
            url: "https://my.freshservice.com/api/v2/tickets/{{ sr_id }}"
            method: GET
            user: "{{ freshservice_api_user }}"
            password: "{{ freshservice_api_password }}"
            headers:
              Content-Type: "application/json"
            validate_certs: no
          register: sr_details

        - name: Validate SR Fields
          fail:
            msg: "Missing required fields in SR ID: {{ sr_id }}"
          when: sr_details.json | default({}) | selectattr('key', 'in', ['environment', 'project', 'service']) | list | length < 3

        - name: Trigger Pod Restart for SR
          ansible.builtin.include_role:
            name: restart_pods
          vars:
            sr_environment: "{{ sr_details.json.environment }}"
            sr_project: "{{ sr_details.json.project }}"
            sr_service: "{{ sr_details.json.service }}"
            sr_restart_all: "{{ sr_details.json.custom_fields.restart_all_pods }}"
            sr_pod_names: "{{ sr_details.json.custom_fields.pod_names }}"













---
- name: Restart Pods in Kubernetes
  hosts: localhost
  gather_facts: no
  tasks:
    - name: Get initial pod count
      shell: >
        oc get pods -n {{ sr_project }} -l app={{ sr_service }} -o jsonpath='{.items | length}'
      register: initial_pod_count
      failed_when: initial_pod_count.rc != 0

    - name: Restart pods
      shell: >
        {% if sr_restart_all %}
        oc delete pod -n {{ sr_project }} -l app={{ sr_service }}
        {% else %}
        oc delete pod -n {{ sr_project }} {{ sr_pod_names.split(',') | join(' ') }}
        {% endif %}
      register: restart_pods_output
      failed_when: restart_pods_output.rc != 0

    - name: Wait for pods to stabilize
      shell: >
        oc wait --for=condition=Ready pods -n {{ sr_project }} -l app={{ sr_service }} --timeout=300s
      register: wait_result
      failed_when: wait_result.rc != 0

    - name: Get final pod count
      shell: >
        oc get pods -n {{ sr_project }} -l app={{ sr_service }} -o jsonpath='{.items | length}'
      register: final_pod_count
      failed_when: final_pod_count.rc != 0

    - name: Validate pod count
      fail:
        msg: "Pod count mismatch! Initial: {{ initial_pod_count.stdout }}, Final: {{ final_pod_count.stdout }}"
      when: initial_pod_count.stdout != final_pod_count.stdout
