---
- name: Get details for the incident
  uri:
    url: "{{ api_url }}/{{ sr_id }}/requested_items"
    method: GET
    url_username: "{{ auth_username }}"
    url_password: "{{ auth_password }}"
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

- name: Debug extracted SR details
  debug:
    msg: "Extracted details: {{ extracted_info }}"

- name: Restart Pods for the SR
  ansible.builtin.include_role:
    name: restart_pods
  vars:
    sr_environment: "{{ extracted_info.environment }}"
    sr_project: "{{ extracted_info.name_of_the_project }}"
    sr_service: "{{ extracted_info.name_of_the_service }}"
    sr_restart_all: "{{ extracted_info.restart_all_pods | default(false) }}"
    sr_pod_names: "{{ extracted_info.pod_names | default('') }}"







---
- name: Fetch SR Details and Restart Pods
  hosts: localhost
  gather_facts: no
  vars:
    api_url: "https://amexgbt.freshservice.com/api/v2/tickets"
    auth_username: "qdIwrrVoGVjaN4HCpgC"
    auth_password: "<your_password_here>"
  tasks:
    - name: Fetch SR IDs
      uri:
        url: "{{ api_url }}"
        method: GET
        url_username: "{{ auth_username }}"
        url_password: "{{ auth_password }}"
        return_content: yes
        status_code: 200
        validate_certs: no
        body_format: json
        force_basic_auth: yes
        follow_redirects: all
      register: sr_ids_response
      failed_when: sr_ids_response.status != 200

    - name: Extract SR IDs from the response
      set_fact:
        sr_ids: "{{ sr_ids_response.json.tickets | map(attribute='id') | list }}"

    - name: Process each SR
      include_tasks: process_sr.yml
      with_items: "{{ sr_ids }}"
      loop_control:
        loop_var: sr_id
      vars:
        sr_id: "{{ sr_id }}"

