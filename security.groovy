{{
        ticket_data.json.requested_items | map(attribute='custom_fields') | 
        map('extract', ['environment', 'account_to_be_unlocked']) |
        map('combine', [{'cluster': item.0, 'username': item.1, 'action': 'unlock'} for item in zip(_, _)]) | list
      }}
