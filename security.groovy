- name: Parse XML and extract email
      set_fact:
xmlparse | xmlselect('//webusers/webuser/email') | first }}
    - name: Print extracted email
      debug:
        msg: "Extracted email: {{ extracted_email }}"
