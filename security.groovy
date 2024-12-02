---
- name: Tasks for DEV_NONCDE
  amazon.aws.elb_application_lb:
    name: "{{ alb_name }}"
    state: present
    region: "{{ aws_region }}"
    subnets:
      - "subnet-0744c28f0d343e071"
      - "subnet-04b3546bad5bb8fa1"
      - "subnet-09456a7aa413a0fa6"
    security_groups:
      - "sg-00cfac7b8c78f4014"
    scheme: "{{ alb_scheme }}"
    ip_address_type: ipv4
    listeners:
      - Protocol: HTTP
        Port: 80
        DefaultActions:
          - Type: forward
            TargetGroupName: Test-Target-group
    when: alb_scheme == 'internal'

- name: Tasks for Internet-Facing ALB
  amazon.aws.elb_application_lb:
    name: "{{ alb_name }}"
    state: present
    region: "{{ aws_region }}"
    subnets:
      - "subnet-0744c28f0d343e071"
      - "subnet-04b3546bad5bb8fa1"
      - "subnet-09456a7aa413a0fa6"
    security_groups:
      - "epaas30-internet-urls"
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
    certificate_arn: "{{ lookup('aws_acm', 'certificate_arn', domain_name=alb_domain_name) }}"
