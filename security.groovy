accounts_info: "{{ ticket_data.results |
      map(attribute='json.requested_items') |
      map('extract', ['custom_fields.environment', 'custom_fields.platform', 'custom_fields.account_name_to_be_unlocked']) |
      map(dict(cluster=item.environment ?? '', username=item.platform ?? '', action='unlock')) |
      list }}"
