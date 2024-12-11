---
- name: Configure ALB for DEV_NONCDE
  hosts: localhost
  gather_facts: no

  tasks:
    - name: Create internal ALB
      amazon.aws.elb_application_lb:
        name: "{{ alb_name }}"
        state: present
        region: "{{ aws_region }}"
        subnets:
          - "subnet-0a123456789abcde1"
          - "subnet-0b123456789abcde2"
        security_groups:
          - "sg-0c123456789abcde3"
        scheme: "{{ alb_scheme }}"
        ip_address_type: ipv4
        listeners:
          - Protocol: HTTP
            Port: 80
            DefaultActions:
              - Type: forward
                TargetGroupName: Test-Target-group
      when: alb_scheme == 'internal'

    - name: Create internet-facing ALB
      amazon.aws.elb_application_lb:
        name: "{{ alb_name }}"
        state: present
        region: "{{ aws_region }}"
        subnets:
          - "subnet-0a123456789abcde1"
          - "subnet-0b123456789abcde2"
        security_groups:
          - "sg-0c123456789abcde3"
        scheme: "{{ alb_scheme }}"
        ip_address_type: ipv4
        listeners:
          - Protocol: HTTPS
            Port: 443
            DefaultActions:
              - Type: forward
                TargetGroupName: Test-Target-group
            SslPolicy: ELBSecurityPolicy-2016-08
            CertificateArn: "{{ certificate_arn }}"
      when: alb_scheme == 'internet-facing'

  vars:
    certificate_arn: "{{ lookup('aws_acm', 'certificate_arn', domain_name=application_name) }}"
