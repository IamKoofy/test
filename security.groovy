- name: Write API response to a temporary file
      copy:
        content: "{{ api_response.content }}"
        dest: /tmp/api_response.xml

    - name: Extract email from XML using xmllint
      shell: "xmllint --xpath 'string(//webusers/webuser/email)' /tmp/api_response.xml"
      register: email_output
      failed_when: email_output.rc != 0
