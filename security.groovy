[defaults]
callback_plugins = ./callback_plugins

[callback_whitelist]
callbacks_enabled = cribl_callback




---
- name: Test Callback Plugin for Cribl
  hosts: localhost
  tasks:
    - name: Print a message
      ansible.builtin.debug:
        msg: "This is a test for Cribl callback"
