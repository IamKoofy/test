- name: Parse XML and extract email
      set_fact:
        extracted_email: "{{ api_response.content | from_xml | json_query('webusers.webuser.email') }}"

    - name: Print extracted email
      debug:
        msg: "Extracted email: {{ extracted_email }}"
