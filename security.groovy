---
- name: Automate copy model folder to PV each sprint release
  hosts: localhost
  gather_facts: false

  vars:
    freshservice_domain: "yourcompany"
    freshservice_api_key: "{{ lookup('env', 'FS_API_KEY') }}"
    change_ticket_id: "12345"
    extraction_folder: "/tmp"
    oc_project_name: "hotelreshop"
    oc_project_path: "/app/projects/HotelReshop/"
    zip_filename: "model.zip"  # Desired name after downloading

  tasks:
    - name: Get attachments from Freshservice change ticket
      uri:
        url: "https://{{ freshservice_domain }}.freshservice.com/api/v2/changes/{{ change_ticket_id }}/attachments"
        method: GET
        headers:
          Authorization: "Basic {{ freshservice_api_key | b64encode }}"
        return_content: yes
        status_code: 200
      register: attachments_response

    - name: Extract the ZIP download URL from response
      set_fact:
        zip_url: "{{ attachments_response.json.attachments | selectattr('content_type', 'equalto', 'application/zip') | map(attribute='attachment_url') | list | first }}"

    - name: Fail if no ZIP attachment found
      fail:
        msg: "No ZIP file found in the change ticket {{ change_ticket_id }}"
      when: zip_url is not defined

    - name: Download the ZIP file from Freshservice
      uri:
        url: "{{ zip_url }}"
        method: GET
        headers:
          Authorization: "Basic {{ freshservice_api_key | b64encode }}"
        dest: "{{ extraction_folder }}/{{ zip_filename }}"
        follow_redirects: all
        force_basic_auth: yes
        status_code: 200
      register: download_result

    - name: Unzip Model Directory
      unarchive:
        src: "{{ extraction_folder }}/{{ zip_filename }}"
        dest: "{{ extraction_folder }}"
        remote_src: false 

    - name: Login to OpenShift
      command: "oc login {{ server }} --token={{ token }}"
      no_log: true

    - name: Switch to the test project
      command: "oc project {{ oc_project_name }}"

    - name: List the pods
      command: "oc get pods"
      register: pods_output

    - name: Copy the model directory to a pod containing 'rasanlu'
      shell: >
        oc cp ./{{ zip_filename | replace('.zip', '') }}
        {{ item.split()[0] }}:{{ oc_project_path }}
      args:
        chdir: "{{ extraction_folder }}"
      loop: "{{ pods_output.stdout_lines }}"
      when: "'rasanlu' in item"
