    - name: Get OpenShift Token from Custom Credential
      set_fact:
        openshift_credentials: "{{ lookup('ansible.builtin.credentials', 'Your Custom Credential Name', wantlist=True) }}"

    - name: Extract OpenShift Token
      set_fact:
        openshift_token: "{{ openshift_credentials.token }}"

    - name: Login to OpenShift
      ansible.builtin.command: "oc login --server={{ server }} --token={{ openshift_token }}"
