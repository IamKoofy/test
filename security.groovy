- name: Restart pods one by one (sequentially)
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
      loop: "{{ pods_to_restart }}"
      loop_control:
        loop_var: pod_name
      block:
        - name: Delete pod {{ pod_name }}
          shell: oc delete pod {{ pod_name }} -n {{ sr_project }}
          register: delete_result
          failed_when: delete_result.rc != 0

        - name: Wait for new pod to become ready
          shell: >
            oc wait --for=condition=Ready pod -n {{ sr_project }} -l deploymentconfig={{ sr_service }} --timeout=300s
          register: wait_ready
          failed_when: wait_ready.rc != 0
