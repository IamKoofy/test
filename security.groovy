   - name: Check pod status
      command: "kubectl get pod -n your_namespace {{ pod_name }} -o jsonpath='{.status.phase}'"
      register: pod_status_output
      changed_when: false
      ignore_errors: true

    - name: Run shell script to check log status
      command: "/path/to/your/shell/script.sh '{{ LOG_PATH }}' '{{ pod_name }}' '{{ LOG_FILE }}'"
      register: log_check_result
      ignore_errors: true

    - name: Send email notification if no logs found or pod is not running
      mail:
        host: your_smtp_server
        port: your_smtp_port
        username: your_smtp_username
        password: your_smtp_password
        to: recipient@example.com
        subject: "Alert: Pod {{ pod_name }} Status"
        body: |
          {% if log_check_result.rc != 0 %}
          The log monitoring script detected no logs for pod {{ pod_name }} within the last 2 minutes.
          {% endif %}
          {% if pod_status_output.stdout != 'Running' %}
          The pod {{ pod_name }} is not running. Current status: {{ pod_status_output.stdout }}.
          {% endif %}
      when: log_check_result.rc != 0 or pod_status_output.stdout != 'Running'
