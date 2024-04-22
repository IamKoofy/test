check pod status:

- name: Check Pod Status
  k8s_info:
    api_version: v1
    kind: Pod
    namespace: "{{ namespace }}"
    name: "{{ pod_name }}"
  register: pod_info

- name: Debug Pod Info
  debug:
    var: pod_info


- name: Restart Pod
  k8s:
    definition:
      apiVersion: v1
      kind: Pod
      metadata:
        name: "{{ pod_name }}"
        namespace: "{{ namespace }}"
      spec:
        containers:
        - name: "{{ pod_name }}" 
    state: restarted


- name: Wait for Pod to Be Running
  wait_for:
    timeout: "{{ pod_restart_timeout }}"
    sleep: 5
    host: "{{ pod_name }}"
    port: 80
    state: started


main.yml
pod_restart_timeout: 300 
