- name: Login to OpenShift
  command: >
    oc login {{ oc_login_url }} --token={{ token }}
  register: oc_login_result
  ignore_errors: yes
  changed_when: oc_login_result.rc != 0

- name: Get all namespaces (raw JSON)
  command: oc get ns -o json
  register: namespaces_json
  when: oc_login_result.rc == 0
  changed_when: false

- name: Extract namespaces with label project-owner = gbt
  set_fact:
    namespaces: >-
      {{
        namespaces_json.stdout | from_json |
        json_query("items[?metadata.labels.'project-owner'=='gbt'].metadata.name")
      }}
  when: oc_login_result.rc == 0

- name: Debug matching namespaces
  debug:
    msg: "{{ namespaces }}"
  when: oc_login_result.rc == 0

- name: Loop through each matching namespace
  include_tasks: backup_namespace.yml
  loop: "{{ namespaces }}"
  loop_control:
    loop_var: namespace
  when: oc_login_result.rc == 0
