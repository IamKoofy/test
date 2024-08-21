accounts_info: "{{ ticket_data.requested_items | 
      map(attribute='custom_fields') | 
      map(dict(cluster=.custom_fields.environment, username=.custom_fields.account_name_to_be_unlocked, action='unlock')) | 
      list }}"
