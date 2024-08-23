 - name: Set fact for incident ID
      set_fact:
        incident_id_fact: "{{ item }}"  # Store the incident ID in a fact
      loop: "{{ incident_ids }}"
      loop_control:
        label: "{{ item }}"
