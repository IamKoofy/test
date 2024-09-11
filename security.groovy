---
- name: ALB Create
  hosts: localhost
  gather_facts: no

  vars_files:
    - aws_credentials.yml
    - vars/vpc_credentials.yml

  tasks:
    # Assuming alb_name and playbook_choice are passed as extra vars from the previous step
    - name: Set ALB scheme based on input
      set_fact:
        alb_scheme: "{{ 'internet-facing' if alb_type == 'internet' else 'internal' }}"

    - name: Set AWS credentials based on VPC ID
      set_fact:
        aws_credential_key: "{{ vpc_credentials[VPC_ID] }}"

    - name: Set AWS access and secret keys
      set_fact:
        aws_access_key: "{{ aws_credential_key.aws_access_key }}"
        aws_secret_key: "{{ aws_credential_key.aws_secret_key }}"

    - name: Debug output of parameters (for troubleshooting)
      debug:
        msg: |
          ALB Name: {{ alb_name }}
          Environment: {{ playbook_choice }}
          AWS Region: {{ aws_region }}
          ALB Scheme: {{ alb_scheme }}
          VPC ID: {{ VPC_ID }}
          Security Groups: {{ security_groups }}

    # Execute the specific sub-playbook based on the environment passed
    - name: Execute sub-playbook based on environment
      include_tasks: "tasks_for_{{ playbook_choice }}.yml"
      vars:
        alb_name: "{{ alb_name }}"
        VPC_ID: "{{ VPC_ID }}"
        alb_scheme: "{{ alb_scheme }}"
        aws_region: "{{ aws_region }}"
        security_groups: "{{ security_groups }}"
      when: playbook_choice in ['DEV_CDE', 'DEV_NONCDE', 'QA_CDE', 'QA_NONCDE', 'PROD_CDE', 'PROD_NONCDE']
