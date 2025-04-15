- name: Extract namespaces where project-owner == "gbt"
  set_fact:
    namespaces: >-
      {{ namespaces_json.stdout | from_json
        | json_query('items[*].{name: metadata.name, labels: metadata.labels}') 
        | selectattr('labels.project-owner', 'equalto', 'gbt') 
        | map(attribute='name') | list }}
  when: oc_login_result.rc == 0
