# roles/poll_freshservice/tasks/main.yml
- name: Poll Freshservice API for pod restart tickets
  uri:
    url: "https://freshservice.yourcompany.com/api/v2/tickets?query=tag:'pod_restart'"
    method: GET
    headers:
      Authorization: "Bearer {{ freshservice_api_token }}"
    return_content: yes
  register: freshservice_response

- name: Parse tickets
  set_fact:
    tickets: "{{ freshservice_response.json.tickets }}"















# roles/process_ticket/tasks/main.yml
- name: Extract ticket details
  set_fact:
    environment: "{{ ticket.custom_fields.environment }}"
    project: "{{ ticket.custom_fields.project_name }}"
    service: "{{ ticket.custom_fields.service_name }}"
    restart_all_pods: "{{ ticket.custom_fields.restart_all_pods | default(false) }}"

- name: Validate ticket data
  fail:
    msg: "Invalid ticket details. Missing project or service."
  when: project is not defined or service is not defined

- name: Set OpenShift login URL based on environment
  set_fact:
    openshift_url: >
      {% if environment == 'cde' %}
      https://api.cde.openshift.yourcompany.com
      {% else %}
      https://api.non-cde.openshift.yourcompany.com
      {% endif %}









# roles/restart_pods/tasks/main.yml
- name: Login to OpenShift
  shell: >
    oc login --token={{ openshift_token }} --server={{ openshift_url }}
  register: login_result
  failed_when: "'Login successful' not in login_result.stdout"

- name: Get pods for the service
  shell: >
    oc get pods -n {{ project }} -l app={{ service }} -o json
  register: pods_result

- name: Parse pod names
  set_fact:
    pod_names: "{{ pods_result.stdout | from_json | json_query('items[*].metadata.name') }}"

- name: Restart all pods if required
  shell: >
    oc delete pod {{ item }} -n {{ project }}
  with_items: "{{ pod_names }}"
  when: restart_all_pods | bool

- name: Restart the first pod if not restarting all
  shell: >
    oc delete pod {{ pod_names[0] }} -n {{ project }}
  when: not restart_all_pods | bool










# roles/process_ticket/tasks/mark_as_processed.yml
- name: Mark ticket as processed
  uri:
    url: "https://freshservice.yourcompany.com/api/v2/tickets/{{ ticket.id }}"
    method: PUT
    headers:
      Authorization: "Bearer {{ freshservice_api_token }}"
    body:
      status: 4  # Closed status
    status_code: 200





# playbook.yml
- name: Automate ROSA Pod Restarts
  hosts: localhost
  gather_facts: no
  vars:
    freshservice_api_token: "your_freshservice_api_token"
    openshift_token: "your_openshift_token"
  tasks:
    - name: Poll Freshservice for tickets
      include_role:
        name: poll_freshservice

    - name: Process each ticket
      include_role:
        name: process_ticket
      with_items: "{{ tickets }}"
      loop_control:
        loop_var: ticket

    - name: Restart pods based on ticket details
      include_role:
        name: restart_pods

    - name: Mark ticket as processed
      include_role:
        name: process_ticket/tasks/mark_as_processed.yml
      with_items: "{{ tickets }}"
      loop_control:
        loop_var: ticket
