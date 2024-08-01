- name: Set API URL for QA environment
      set_fact:
        api_url: "https://aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/{{ username }}"
      when: environment == 'QA'

    - name: Set API URL for PROD environment
      set_fact:
        api_url: "https://prod.aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/{{ username }}"
      when: environment == 'PROD'

    - name: Set API URL for DEV environment (default)
      set_fact:
        api_url: "https://dev.aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/{{ username }}"
      when: environment not in ['QA', 'PROD']
