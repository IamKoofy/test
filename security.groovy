accounts_info: "{{ ticket_data.requested_items | 
      map(attribute='custom_fields') | 
      map(dict(cluster=.environment, username=.account_name_to_be_unlocked, action='unlock')) | 
      list }}"
