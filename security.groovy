---
- name: Get details for the incident
  uri:
    url: "{{ api_url }}/{{ sr_id }}/requested_items"
    method: GET
    url_username: "{{ auth_username }}"
    return_content: yes
    status_code: 200
    validate_certs: no
    body_format: json
    force_basic_auth: yes
    follow_redirects: all
  register: sr_details
  failed_when: sr_details.status != 200

- name: Parse the JSON content from ticket data
  set_fact:
    json_data: "{{ sr_details.json | default({}) }}"

- name: Extract fields from the JSON data
  set_fact:
    extracted_info:
      environment: "{{ json_data.requested_items[0].custom_fields.environment }}"
      project: "{{ json_data.requested_items[0].custom_fields.name_of_the_project }}"
      service: "{{ json_data.requested_items[0].custom_fields.name_of_the_service }}"
      restart_all: "{{ json_data.requested_items[0].custom_fields.should_we_restart_all_pods_or_one_pod_at_a_time | lower }}"
      pod_names: "{{ json_data.requested_items[0].custom_fields.name_of_the_pod | default('') }}"

- name: Debug extracted SR details
  debug:
    msg: "Extracted details: {{ extracted_info }}"

- name: Restart Pods for the SR
  include_tasks: restart_pods.yml
  vars:
    sr_environment: "{{ extracted_info.environment }}"
    sr_project: "{{ extracted_info.project }}"
    sr_service: "{{ extracted_info.service }}"
    sr_restart_all: "{{ extracted_info.restart_all }}"
    sr_pod_names: "{{ extracted_info.pod_names }}"









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
    oc get pods -n {{ sr_project }} -l app={{ sr_service }} -o jsonpath="{.items[?(@.status.phase=='Running')].metadata.name}" | wc -w
  register: initial_pod_count
  failed_when: initial_pod_count.rc != 0

- name: Restart pods
  shell: >
    {% if sr_restart_all == 'yes' %}
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
    oc get pods -n {{ sr_project }} -l app={{ sr_service }} -o jsonpath="{.items[?(@.status.phase=='Running')].metadata.name}" | wc -w
  register: final_pod_count
  failed_when: final_pod_count.rc != 0

- name: Validate pod count
  fail:
    msg: "Pod count mismatch! Initial: {{ initial_pod_count.stdout }}, Final: {{ final_pod_count.stdout }}"
  when: initial_pod_count.stdout != final_pod_count.stdout
