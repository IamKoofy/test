- name: Test ALB Creation
  hosts: localhost
  gather_facts: false
  tasks:
    - name: Create ALB
      amazon.aws.elb_application_lb:
        name: "test-alb"
        state: present
        region: "us-east-1"
        subnets:
          - "subnet-0a123456789abcde1"
          - "subnet-0b123456789abcde2"
        security_groups:
          - "sg-0c123456789abcde3"
        scheme: "internal"
        ip_address_type: ipv4
        listeners:
          - Protocol: HTTP
            Port: 80
            DefaultActions:
              - Type: forward
                TargetGroupName: Test-Target-group
        aws_access_key: "YOUR_ACCESS_KEY"
        aws_secret_key: "YOUR_SECRET_KEY"
