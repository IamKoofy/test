- name: Scale down the pod if an exception is found
      shell: oc scale --replicas=0 deployment/amosservice -n amos
      when: exception_check.rc == 0

    - name: Sleep for 10 seconds to allow the pod to terminate fully
      pause:
        seconds: 10
      when: exception_check.rc == 0

    - name: Scale up the pod after sleep
      shell: oc scale --replicas=1 deployment/amosservice -n amos
      when: exception_check.rc == 0
