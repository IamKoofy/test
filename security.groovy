---
- name: Main playbook
  hosts: localhost
  gather_facts: no
  vars_prompt:
    - name: playbook_choice
      prompt: Enter ENVIRONMENT (DEV_CDE, DEV_NONCDE_CREDS, QA_CDE, QA_NONCDE, PROD_CDE, PROD_NONCDE)
      private: no
    - name: VPC_ID
      prompt: "Please enter the VPC ID"
      private: no
    - name: alb_type
      prompt: Please enter the type of ALB (internet or intranet)
      private: no
    - name: aws_region
      prompt: Please enter the AWS Region
      private: no
    - name: security_groups
      prompt: Please enter Security Groups (comma-separated)
      private: no
  
  vars_files:
    - aws_credentials.yml
    - vars/vpc_credentials.yml
  
  tasks:
    - name: Set ALB scheme based on user input
      set_fact:
        alb_scheme: "{{ 'internet-facing' if alb_type == 'internet' else 'internal' }}"

    - name: Set AWS credentials based on VPC ID
      set_fact:
        aws_credential_key: "{{ vpc_credentials[VPC_ID] }}"

    - name: Set AWS access and secret keys
      set_fact:
        aws_access_key: "{{ aws_credential_key.aws_access_key }}"
        aws_secret_key: "{{ aws_credential_key.aws_secret_key }}"
        aws_region: "{{ aws_region }}"

    - name: Execute sub-playbook based on choice
      include_tasks: "tasks_for_{{ playbook_choice }}.yml"
      when: playbook_choice in ['DEV_CDE', 'DEV_NONCDE_CREDS', 'QA_CDE', 'QA_NONCDE', 'PROD_CDE', 'PROD_NONCDE']
