---
- name: Install Ansible Collection
  hosts: localhost
  gather_facts: false

  tasks:
    - name: Install MyCollection
      ansible.builtin.shell:
        cmd: "ansible-galaxy collection install my_namespace.my_collection"
      become: true
