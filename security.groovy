---
- name: Cleanup old backups in epaas-backup repository and commit changes
  hosts: localhost
  gather_facts: no
  tasks:
  
    # Clone the GitHub repository into a local directory inside the ROSA pod
    - name: Clone the epaas-backup GitHub repository
      git:
        repo: 'https://github.com/AETEST/epaas-backup.git'
        dest: '/tmp/epaas-backup'  # Path where the repo will be cloned inside the pod
        clone: yes
        update: yes

    # Cleanup for non_cde/prod
    - name: Delete old backup directories in non_cde/prod
      find:
        paths: "/tmp/epaas-backup/non_cde/prod"
        recurse: yes
        file_type: directory
        age: 7d
      register: old_backups_non_cde_prod

    - name: Remove old backups in non_cde/prod
      file:
        path: "{{ item.path }}"
        state: absent
      loop: "{{ old_backups_non_cde_prod.files }}"
      when: old_backups_non_cde_prod.matched > 0

    # Cleanup for non_cde/dev
    - name: Delete old backup directories in non_cde/dev
      find:
        paths: "/tmp/epaas-backup/non_cde/dev"
        recurse: yes
        file_type: directory
        age: 7d
      register: old_backups_non_cde_dev

    - name: Remove old backups in non_cde/dev
      file:
        path: "{{ item.path }}"
        state: absent
      loop: "{{ old_backups_non_cde_dev.files }}"
      when: old_backups_non_cde_dev.matched > 0

    # Cleanup for non_cde/qa
    - name: Delete old backup directories in non_cde/qa
      find:
        paths: "/tmp/epaas-backup/non_cde/qa"
        recurse: yes
        file_type: directory
        age: 7d
      register: old_backups_non_cde_qa

    - name: Remove old backups in non_cde/qa
      file:
        path: "{{ item.path }}"
        state: absent
      loop: "{{ old_backups_non_cde_qa.files }}"
      when: old_backups_non_cde_qa.matched > 0

    # Cleanup for cde/prod
    - name: Delete old backup directories in cde/prod
      find:
        paths: "/tmp/epaas-backup/cde/prod"
        recurse: yes
        file_type: directory
        age: 7d
      register: old_backups_cde_prod

    - name: Remove old backups in cde/prod
      file:
        path: "{{ item.path }}"
        state: absent
      loop: "{{ old_backups_cde_prod.files }}"
      when: old_backups_cde_prod.matched > 0

    # Cleanup for cde/dev
    - name: Delete old backup directories in cde/dev
      find:
        paths: "/tmp/epaas-backup/cde/dev"
        recurse: yes
        file_type: directory
        age: 7d
      register: old_backups_cde_dev

    - name: Remove old backups in cde/dev
      file:
        path: "{{ item.path }}"
        state: absent
      loop: "{{ old_backups_cde_dev.files }}"
      when: old_backups_cde_dev.matched > 0

    # Cleanup for cde/qa
    - name: Delete old backup directories in cde/qa
      find:
        paths: "/tmp/epaas-backup/cde/qa"
        recurse: yes
        file_type: directory
        age: 7d
      register: old_backups_cde_qa

    - name: Remove old backups in cde/qa
      file:
        path: "{{ item.path }}"
        state: absent
      loop: "{{ old_backups_cde_qa.files }}"
      when: old_backups_cde_qa.matched > 0

    # Commit and push changes to GitHub repository
    - name: Commit changes to GitHub
      git:
        repo: 'https://github.com/AETEST/epaas-backup.git'
        dest: '/tmp/epaas-backup'
        commit: 'Cleanup: Removed old backups older than 7 days'
        push: yes
        user: 'Your Name'
        email: 'your-email@example.com'
        ssh_key_file: '/path/to/your/ssh/key'  # Optional, if using SSH keys for GitHub access
