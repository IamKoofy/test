    - name: Download Model Directory from Nexus
      ansible.builtin.uri:
        url: "{{ nexus_base_url }}/{{ nexus_repository_path }}/{{ model_directory }}"
        dest: "{{ extraction_folder }}/"
        follow_redirects: all
      register: download_result

    - name: Fail if download failed
      ansible.builtin.fail:
        msg: "Failed to download the Model Directory from Nexus"
      when: download_result.status != 200
