artifact_name: MyApp.zip
artifact_repo_url: "https://github.com/org/repo/releases/download/v1.0.0/{{ artifact_name }}"
app_pool: MyAppPool
site_name: MyWebsite
component_name: MyComponent
virtual_dir: /MyComponent
deploy_path: "C:\\inetpub\\wwwroot\\MyComponent"
temp_download_dir: "C:\\Temp\\deploy"





📄 roles/iis_deploy/tasks/main.yml


---
- name: Ensure temp directory exists
  win_file:
    path: "{{ temp_download_dir }}"
    state: directory

- name: Download artifact zip
  win_get_url:
    url: "{{ artifact_repo_url }}"
    dest: "{{ temp_download_dir }}\\{{ artifact_name }}"
    force: yes

- name: Unzip artifact
  win_unzip:
    src: "{{ temp_download_dir }}\\{{ artifact_name }}"
    dest: "{{ deploy_path }}"
    remote_src: yes
    removes: "{{ deploy_path }}\\web.config"

- name: Configure IIS application
  win_iis_webapplication:
    name: "{{ virtual_dir }}"
    site: "{{ site_name }}"
    physical_path: "{{ deploy_path }}"
    application_pool: "{{ app_pool }}"
    state: started

- name: Recycle App Pool
  win_iis_webapppool:
    name: "{{ app_pool }}"
    state: restarted

- name: Start App Pool
  win_iis_webapppool:
    name: "{{ app_pool }}"
    state: started







---
- name: Deploy IIS App from GitHub Artifact
  hosts: iis_targets
  gather_facts: false
  vars_files:
    - inventories/production/vars.yml
  roles:
    - iis_deploy




