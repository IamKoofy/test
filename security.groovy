---
- name: Get pod name and trigger email notification if no logs found
  hosts: localhost
  gather_facts: no
  tasks:
    - name: Fetch pod name
      shell: kubectl get pods -n your_namespace -l app={{ your_app_label }} -o jsonpath="{.items[0].metadata.name}"
      register: pod_name_output
      changed_when: false
      ignore_errors: true

    - set_fact:
        pod_name: "{{ pod_name_output.stdout }}"
      when: pod_name_output.rc == 0

    - name: Run shell script to check log status
      command: "/path/to/your/shell/script.sh /data/logs/{{ PTC }}/{{ pod_name }} {{ log_file }}"
      register: log_check_result
      ignore_errors: true

    - name: Send email notification if no logs found
      mail:
        host: your_smtp_server
        port: your_smtp_port
        username: your_smtp_username
        password: your_smtp_password
        to: recipient@example.com
        subject: "No Logs Found for Pod {{ pod_name }}"
        body: "The log monitoring script detected no logs for pod {{ pod_name }} within the last 2 minutes."
      when: log_check_result.rc != 0
