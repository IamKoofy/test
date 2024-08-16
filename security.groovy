- name: Get incidents from filter view
  uri:
    url: "https://test.freshservice.com/helpdesk/tickets/view/1000098988?format=json"
    method: GET
    headers:
      Authorization: "Bearer YOUR_API_KEY"
    return_content: yes
    status_code: 200
  register: incidents_response

- name: Parse incident IDs from response
  set_fact:
    incident_ids: "{{ incidents_response.json | map(attribute='display_id') | list }}"
  when: incidents_response.json | length > 0






- name: Loop through incidents to gather details
  set_fact:
    incident_info_list: []

- name: Get details for each incident
  uri:
    url: "https://test.freshservice.com/api/v2/tickets/{{ item }}/requested_items"
    method: GET
    headers:
      Authorization: "Bearer YOUR_API_KEY"
    return_content: yes
    status_code: 200
  with_items: "{{ incident_ids }}"
  register: ticket_details

- name: Parse account_name, Environment, and platform for each incident
  set_fact:
    incident_info_list: "{{ incident_info_list + [{ 'incident_id': item.item, 'account_name': item.json.requested_items.account_name, 'environment': item.json.requested_items.environment, 'platform': item.json.requested_items.platform }] }}"
  with_items: "{{ ticket_details.results }}"






- name: Trigger AWX template if incidents are found
  awx_job_template:
    template: "Unlock Accounts"
    organization: "Your Org"
    extra_vars:
      incident_info_list: "{{ incident_info_list }}"
  when: incident_ids | length > 0
