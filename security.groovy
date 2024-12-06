---
- name: Cleanup old backup directories
  hosts: localhost
  gather_facts: false
  vars:
    backup_base_path: "/tmp/epaas-backup/non_cde/dev"  # Adjust as needed
    retention_days: 7

  tasks:
    - name: List all directories in the backup base path
      command: ls -1 "{{ backup_base_path }}"
      register: dir_list
      changed_when: false

    - name: Filter directories with a valid date pattern
      set_fact:
        backup_dirs: >-
          {{
            dir_list.stdout_lines | select('match', '_backup_\\d{4}-\\d{2}-\\d{2}$')
          }}

    - name: Parse directory dates and find old backups
      set_fact:
        old_backups: >-
          {{
            backup_dirs | map('regex_replace', '.*_backup_(\\d{4}-\\d{2}-\\d{2})', '\\1') |
            map('to_datetime', '%Y-%m-%d') |
            select('lt', (now() - retention_days | int | timedelta(days=1))) |
            map('strftime', '%Y-%m-%d') |
            map('regex_replace', '(.*)', backup_base_path ~ '/' ~ '\\1')
          }}

    - name: Debug backup directories and old backups
      debug:
        msg: >
          Backup Directories: {{ backup_dirs }}
          Old Backups: {{ old_backups }}

    - name: Delete old backup directories
      file:
        path: "{{ item }}"
        state: absent
      loop: "{{ old_backups }}"
      when: old_backups | length > 0
