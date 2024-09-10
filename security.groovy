---
- name: Query JIRA issue for custom fields
  hosts: localhost
  gather_facts: no
  vars:
    jira_api_url: "https://your-jira-instance.atlassian.net/rest/api/2/issue"
    jira_issue_key: "SUPPORT-123"  # Replace with the actual JIRA issue key
    jira_encoded_api_key: "your_base64_encoded_api_key_here"  # Replace with the base64 encoded API key
  tasks:
    - name: Fetch JIRA issue details
      uri:
        url: "{{ jira_api_url }}/{{ jira_issue_key }}"
        method: GET
        headers:
          Authorization: "Basic {{ jira_encoded_api_key }}"
          Accept: "application/json"
        return_content: yes
      register: jira_response

    - name: Parse JIRA response and extract custom fields
      set_fact:
        customfield_18234: "{{ jira_response.json.fields.customfield_18234 }}"
        customfield_1888: "{{ jira_response.json.fields.customfield_1888 }}"
        customfield_2888: "{{ jira_response.json.fields.customfield_2888 }}"

    - name: Display extracted custom fields
      debug:
        msg: |
          customfield_18234: {{ customfield_18234 }}
          customfield_1888: {{ customfield_1888 }}
          customfield_2888: {{ customfield_2888 }}

