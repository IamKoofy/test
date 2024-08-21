- name: Parse the JSON content from ticket_data
  set_fact:
    json_data: "{{ ticket_data.results[0].content | from_yaml }}"

- name: Extract fields from the JSON data
  set_fact:
    extracted_info: >-
      {{
        json_data.requested_items[0].custom_fields | dict2items |
        selectattr('key', 'in', ['environment', 'platform', 'account_name_to_be_unlocked']) |
        items2dict
      }}
