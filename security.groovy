---
# tasks/restart_pods.yml

- name: Login to OpenShift
  shell: >
    {% if sr_environment == 'CDE' %}
    oc login --token={{ cde_api_token }} --server={{ cde_api_url }}
    {% else %}
    oc login --token={{ non_cde_api_token }} --server={{ non_cde_api_url }}
    {% endif %}
  register: oc_login
  failed_when: oc_login.rc != 0

- name: Get initial pod count
  shell: >
    oc get pods -n {{ sr_project }} -l app={{ sr_service }} -o jsonpath='{.items | length}'
  register: initial_pod_count
  failed_when: initial_pod_count.rc != 0

- name: Restart pods
  shell: >
    {% if sr_restart_all %}
    oc delete pod -n {{ sr_project }} -l app={{ sr_service }}
    {% else %}
    oc delete pod -n {{ sr_project }} {{ sr_pod_names.split(',') | join(' ') }}
    {% endif %}
  register: restart_pods_output
  failed_when: restart_pods_output.rc != 0

- name: Wait for pods to stabilize
  shell: >
    oc wait --for=condition=Ready pods -n {{ sr_project }} -l app={{ sr_service }} --timeout=300s
  register: wait_result
  failed_when: wait_result.rc != 0

- name: Get final pod count
  shell: >
    oc get pods -n {{ sr_project }} -l app={{ sr_service }} -o jsonpath='{.items | length}'
  register: final_pod_count
  failed_when: final_pod_count.rc != 0

- name: Validate pod count
  fail:
    msg: "Pod count mismatch! Initial: {{ initial_pod_count.stdout }}, Final: {{ final_pod_count.stdout }}"
  when: initial_pod_count.stdout != final_pod_count.stdout
