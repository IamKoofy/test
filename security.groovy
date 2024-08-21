accounts_info: >-
      {{
        ticket_data.json.requested_items |
        json_query('[*].{cluster: custom_fields.environment, username: custom_fields.account_to_be_unlocked, action: "unlock"}')
      }}
