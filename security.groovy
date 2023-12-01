{
  "name": "Nexus Credential",
  "description": "Custom Credential for Nexus Authentication",
  "kind": "cloud",
  "inputs": [
    {
      "id": "username",
      "label": "Nexus Username",
      "type": "string"
    },
    {
      "id": "password",
      "label": "Nexus Password",
      "secret": true,
      "type": "string"
    }
  ]
}


    - name: Get Nexus Credentials from AWX
      set_fact:
        nexus_credentials: "{{ lookup('ansible.builtin.credentials', 'your_nexus_credential', wantlist=True) }}"

    - name: Download Model Directory ZIP from Nexus with Authentication
      ansible.builtin.uri:
        url: "{{ nexus_base_url }}/{{ nexus_repository_path }}/{{ model_zip_file }}"
        dest: "{{ extraction_folder }}/"
        follow_redirects: all
        user: "{{ nexus_credentials.username }}"
        password: "{{ nexus_credentials.password }}"
        force_basic_auth: yes
      register: download_result
