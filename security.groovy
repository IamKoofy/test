- name: Send email notification for successful backup in production
      mail:
        host: localhost
        port: 25
        subject: "ROSA Backup Completed Successfully for {{ env_var }}"
        body: |
          Backup for the environment {{ env_var }} and namespaces {{ namespaces }} has been completed successfully.
          Backup stored at: {{ backup_location }}
        to: "{{ email_recipients | join(',') }}"
      when: env_var in ['non-cde-prod', 'cde-prod']
      delegate_to: localhost
