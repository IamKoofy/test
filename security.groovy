---
- name: Tasks for DEV_CDE
  amazon.aws.elb_application_lb:
    name: "test-alb"
    state: present
    region: "{{ aws_region }}"
    subnets:
      - "subnet-04aab667b1c223ecd"
      - "subnet-0ba426hgbe16400c"
      - "subnet-07efe88bsdeda2f"
    security_groups: "{{ security_groups.split(',') }}"
    VPC_ID: "{{ VPC_ID }}"
    listeners:
      - Protocol: HTTP
        Port: 80
  register: alb_result

- name: Output ALB DNS name
  debug:
    msg: "ALB DNS Name: {{ alb_result.load_balancer.DNSName }}"
