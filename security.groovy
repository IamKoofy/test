---
- name: Check and restart service based on exceptions
  hosts: localhost
  tasks:
    - name: Log in to ROSA using OC login
      shell: oc login --token=<TOKEN> --server=<API_URL>
      register: login_result

    - name: Get the pod name for the amosservice deployment config
      shell: oc get pods -n amos -o custom-columns=POD_NAME:.metadata.name --no-headers | grep amosservice
      register: pod_name

    - name: Set the EXCEPTION_LOGFILE
      set_fact:
        EXCEPTION_LOGFILE: "{{ pod_name.stdout }}/service.log"

    - name: Run the exception check shell script
      script: roles/ExceptionCheck/files/EXC-checkKEYWORD.sh '{{ EXCEPTION_LOG_PATH }}' '{{ EXCEPTION_STRING }}' '{{ THRESHOLD }}' '{{ EXCEPTION_LOGFILE }}' '{{ INTERVAL }}'
      register: exception_check
      args:
        EXCEPTION_LOG_PATH: /var/logs/amos/RT/json/service
        EXCEPTION_STRING: "Unable to acquire JDBC Connection"
        THRESHOLD: 3
        INTERVAL: 5

    - name: Restart the pod if an exception is found
      shell: |
        oc scale --replicas=0 deployment/amosservice -n amos && oc scale --replicas=1 deployment/amosservice -n amos
      when: exception_check.rc == 0
