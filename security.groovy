    - name: Download Model Directory ZIP from Nexus
      ansible.builtin.uri:
        url: "{{ nexus_base_url }}/{{ nexus_repository_path }}/{{ model_zip_file }}"
        dest: "{{ extraction_folder }}/"
        follow_redirects: all
      register: download_result

    - name: Fail if download failed
      ansible.builtin.fail:
        msg: "Failed to download the Model Directory ZIP from Nexus"
      when: download_result.status != 200

    - name: Unzip Model Directory
      ansible.builtin.unarchive:
        src: "{{ extraction_folder }}/{{ model_zip_file }}"
        dest: "{{ extraction_folder }}"
        remote_src: false  # Important to specify this when unzipping from the control host

