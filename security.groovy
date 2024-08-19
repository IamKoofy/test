- name: Launch AWX job template using API with Basic Auth
  uri:
    url: "https://{{ awx_host }}/api/v2/job_templates/{{ awx_template_id }}/launch/"
    method: POST
    headers:
      Content-Type: "application/json"
    body_format: json
    body:
      extra_vars:
        cluster: "{{ accounts_info.cluster }}"
        username: "{{ accounts_info.username }}"
        action: "{{ accounts_info.action }}"
    status_code: 201
    validate_certs: no  # Set to 'yes' if using valid SSL certificates
    url_username: "{{ awx_username }}"  # Basic Auth username
    url_password: "{{ awx_password }}"  # Basic Auth password
    force_basic_auth: yes
  register: awx_job_result
