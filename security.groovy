{{
        ticket_data.json.requested_items | map(attribute='custom_fields') |
        map('combine', [{'cluster': item.environment, 'username': item.account_name_to_be_unlocked, 'action': 'unlock'}]) | list
      }}
