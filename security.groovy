accounts_info: >-
      accounts_info: >-
      {% if ticket_data.results.json.requested_items is list %}
      {{
        ticket_data.results |
        map(attribute='json.requested_items') |
        map('extract', ['custom_fields.environment', 'custom_fields.platform', 'custom_fields.account_name_to_be_unlocked']) |
        map('combine', [{'cluster': item[0], 'platform': item[1], 'username': item[2], 'action': 'unlock'} for item in _]) |
        list
      }}
      {% else %}
      []  # Empty list if requested_items is not a list
      {% endif %}
