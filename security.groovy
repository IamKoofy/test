---
- name: COPY AND EXECUTE THE scriptp
  script: roles/ExceptionCheck/files/EXC-GetPODS.sh '{{ EXCEPTION_LOG_PATH }}' '{{ TOT_NUMBER_PODS }}' '{{ POD_NAME }}'
  register: scriptp
  changed_when: false

- name: PRINT scriptp STANDARD OUTPUT1
  debug:
    msg: "{{ scriptp.stdout_lines }}"

- name: SET FACT FOR POD LIST
  set_fact:
    pod_list: "{{ scriptp.stdout_lines | reject('match', '^$') | difference(scriptp.stdout_lines | select('search', 'GETPOD_SUCCESS') | list) }}"

- name: EXCEPTION CHECK FOR EACH POD - PTC EPAAS***
  include_role:
    name: ExceptionCheck
    tasks_from: EXC-CheckAPP-EPAAS
  vars:
    EXC_LOG_PATH: "{{ EXCEPTION_LOG_PATH }}"
    EXC_STRINGS: "{{ EXCEPTION_STRINGS }}"
    EXC_PODNAME: "{{ item }}"
    EXC_THRESHOLD: "{{ THRESHOLD }}"
    EXC_LOGFILE: "{{ EXCEPTION_LOGFILE }}"
    EXC_TIME_INTERVAL: "{{ TIME_INTERVAL }}"
    EXC_APP_NAME: "{{ POD_NAME }}"
  when: (item | length > 0) and scriptp.stdout.find("GETPOD_SUCCESS") != -1
  with_items: "{{ pod_list }}"

- name: RUN EXCEPTION SCRIPT AND NOTIFY IF KEYWORD NOT FOUND
  command: "/path/to/your/script.sh '{{ EXCEPTION_LOG_PATH }}' 'inside SMS' '{{ POD_NAME }}' '{{ THRESHOLD }}' '{{ EXCEPTION_LOGFILE }}' 3 '{{ POD_NAME }}'"
  register: exception_script_output

- name: NOTIFY IF KEYWORD NOT FOUND
  mail:
    host: "your_mail_server"
    port: 25
    subject: "Keyword Not Found Alert"
    body: "The keyword 'inside SMS' was not found in the log file within the last 3 hours."
  when: exception_script_output.rc == 1
