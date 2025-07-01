- name: Get details for the incident
  uri:
    url: "{{ api_url }}/{{ sr_id }}/requested_items"
    method: GET
    url_username: "{{ auth_username }}"
    url_password:
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
    extracted_info: >-
      {{
        json_data.requested_items[0].custom_fields
         | dict2items
         | selectattr('key', 'in', ['environment', 'name_of_the_project', 'name_of_the_service'])
         | items2dict
      }}

- name: **Trim spaces from extracted SR input fields**
  set_fact:
    extracted_info:
      environment: "{{ extracted_info.environment | default('') | trim }}"
      name_of_the_project: "{{ extracted_info.name_of_the_project | default('') | trim }}"
      name_of_the_service: "{{ extracted_info.name_of_the_service | default('') | trim }}"

- name: Debug extracted SR details
  debug:
    msg: "Extracted details: {{ extracted_info }}"

- name: Restart Pods for the SR
  include_tasks: restart_pods.yml
  vars:
    sr_environment: "{{ extracted_info.environment }}"
    sr_project: "{{ extracted_info.name_of_the_project }}"
    sr_service: "{{ extracted_info.name_of_the_service }}"
    sr_restart_all: "{{ json_data.requested_items[0].custom_fields.should_we_restart_all_pods_or_one_pod_at_a_time | lower | trim }}"
    sr_pod_names: "{{ json_data.requested_items[0].custom_fields.name_of_the_pod | default('') | trim }}"
