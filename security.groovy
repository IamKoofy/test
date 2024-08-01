tasks:
    - name: Set API URL based on environment
      set_fact:
        api_url: >-
          {{
            'https://aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/' + username
            if environment == 'QA'
            else
            'https://prod.aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/' + username
            if environment == 'PROD'
            else
            'https://dev.aoapi.myaxt.com:8001/go/rest/gacmd/v1/webusers/' + username
          }}

    - name: Debug API URL to verify environment setting
      debug:
        msg: "API URL is set to {{ api_url }}"
