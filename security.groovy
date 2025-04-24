# Restart all pods one by one (sequentially)
- name: Restart pods one by one (all)
  when: sr_restart_all | lower | trim == 'yes'
  block:
    - name: Get list of pod names to restart
      shell: >
        oc get pods -n {{ sr_project }} --selector=deploymentconfig={{ sr_service }} -o jsonpath="{.items[*].metadata.name}"
      register: pod_list_output
      failed_when: pod_list_output.rc != 0

    - name: Set fact for pods list
      set_fact:
        pods_to_restart: "{{ pod_list_output.stdout.split() }}"

    - name: Restart pods one at a time
      include_tasks: restart_single_pod.yml
      loop: "{{ pods_to_restart }}"
      loop_control:
        loop_var: pod_name

# Restart specific pods one by one (sequentially)
- name: Restart specific pods one by one
  when: sr_restart_all | lower | trim != 'yes'
  block:
    - name: Parse provided pod names into list
      set_fact:
        pods_to_restart: "{{ sr_pod_names.split(',') | map('trim') | list }}"

    - name: Restart specified pods one at a time
      include_tasks: restart_single_pod.yml
      loop: "{{ pods_to_restart }}"
      loop_control:
        loop_var: pod_name
