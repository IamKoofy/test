{%- set accounts = [] -%}
      {%- for item in ticket_data.results -%}
        {%- set requested_items = item.json.requested_items -%}
        {%- for req_item in requested_items -%}
          {%- set fields = req_item.custom_fields -%}
          {%- set account_info = {'cluster': fields.environment, 'username': fields.account_name_to_be_unlocked, 'action': 'unlock'} -%}
          {%- do accounts.append(account_info) -%}
        {%- endfor -%}
      {%- endfor -%}
      {{ accounts }}
