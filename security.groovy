---
- name: Cleanup old backup directories
  hosts: localhost
  gather_facts: false
  vars:
    backup_base_path: "/tmp/epaas-backup/non_cde/dev" # Change this as needed
    retention_days: 7

  tasks:
    - name: List all directories in the backup base path
      command: ls -1 "{{ backup_base_path }}"
      register: dir_list
      changed_when: false

    - name: Filter backup directories based on naming pattern
      set_fact:
        backup_dirs: >-
          {{
            dir_list.stdout_lines | select('match', '_backup_\\d{4}-\\d{2}-\\d{2}$') |
            map('regex_search', '.*_(\\d{4}-\\d{2}-\\d{2})') |
            map('first') | list
          }}

    - name: Identify old backups
      set_fact:
        old_backups: >-
          {{
            backup_dirs | select('date_compare', '%Y-%m-%d', now() | strftime('%Y-%m-%d'), "<", retention_days) |
            map('regex_replace', '(.*)', backup_base_path ~ '/' ~ '\\1')
          }}

    - name: Debug old backups
      debug:
        var: old_backups

    - name: Delete old backup directories
      file:
        path: "{{ item }}"
        state: absent
      loop: "{{ old_backups }}"
      when: old_backups | length > 0
