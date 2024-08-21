environment: "{{ ticket_data.json.requested_items[0].custom_fields.environment }}"
    platform: "{{ ticket_data.json.requested_items[0].custom_fields.platform }}"
    account_name_to_be_unlocked: "{{ ticket_data.json.requested_items[0].custom_fields.account_name_to_be_unlocked }}"
