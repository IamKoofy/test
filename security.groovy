- name: Pass variables to workflow
      set_stats:
        data:
          alb_environment: "{{ alb_environment }}"
          alb_name: "{{ alb_name }}"
          application: "{{ application }}"
