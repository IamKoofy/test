{{
        ticket_data.results | map(attribute='json.requested_items') |
        selectattr(0, 'defined') | 
        map(attribute=0) | 
        map(attribute='custom_fields') |
        map('combine', [{'cluster': item.environment, 'username': item.account_name_to_be_unlocked, 'action': 'unlock'}]) | list
      }}
