- name: Pass hardcoded values to AWX template
  awx.awx.job_template_launch:
    name: "{{ awx_template_name }}"  # Your AWX template name here
    extra_vars:
      cluster: "{{ accounts_info.cluster }}"
      username: "{{ accounts_info.username }}"
      action: "{{ accounts_info.action }}"
  register: awx_job_result

- debug:
    msg: "AWX job result: {{ awx_job_result }}"
