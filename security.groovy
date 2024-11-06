import json
import requests
from datetime import datetime
from ansible.plugins.callback import CallbackBase
import os

class CallbackModule(CallbackBase):
    CALLBACK_VERSION = 2.0
    CALLBACK_TYPE = 'notification'
    CALLBACK_NAME = 'log_to_cribl'

    def __init__(self):
        super(CallbackModule, self).__init__()
        self.cribl_endpoint = "https://default.main.vibrant-90msg.cribl.cloud:10070"
        self.auth_token = "9999anf-annnnd-9444k-998885859004"
        self.headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {self.auth_token}'  # Ensure this is correct
        }
        self.user_id = None
        self.user_email = None
        self.template_name = None

    def v2_playbook_on_start(self, playbook):
        # Retrieve AWX user info and print for debugging
        self.user_id, self.user_email = self._get_awx_user_info(playbook)
        print(f"[DEBUG] Retrieved user_id: {self.user_id}")
        print(f"[DEBUG] Retrieved user_email: {self.user_email}")

        self.template_name = playbook._file_name
        print(f"[DEBUG] Template name: {self.template_name}")

        log_entry = {
            "user_id": self.user_id,
            "user_email": self.user_email,
            "template_name": self.template_name,
            "event_type": "playbook_start",
            "timestamp": datetime.utcnow().isoformat()
        }
        self.send_to_cribl(log_entry)

    def v2_playbook_on_stats(self, stats):
        # Summarize stats for logging and print for debugging
        summary = {host: stats.summarize(host) for host in stats.processed}
        print(f"[DEBUG] Summary: {summary}")

        log_entry = {
            "user_id": self.user_id,
            "user_email": self.user_email,
            "template_name": self.template_name,
            "event_type": "playbook_end",
            "timestamp": datetime.utcnow().isoformat(),
            "summary": summary
        }
        self.send_to_cribl(log_entry)

    def send_to_cribl(self, log_data):
        print(f"[DEBUG] Sending log data to Cribl: {log_data}")
        try:
            response = requests.post(self.cribl_endpoint, headers=self.headers, json=log_data, verify=False)
            response.raise_for_status()  # Raise an error for bad responses
            print(f"[DEBUG] Response from Cribl: {response.status_code} - {response.text}")
        except requests.exceptions.HTTPError as e:
            print(f"[ERROR] HTTP error occurred: {str(e)}")
        except requests.exceptions.RequestException as e:
            print(f"[ERROR] Request error: {str(e)}")

    def _get_awx_user_info(self, playbook):
        """
        Get the AWX user info (user_id and email) using AWX meta variables.
        These variables should be available in extra_vars or host_vars.
        """
        user_id = None
        user_email = None

        # Check if the playbook has the AWX user information
        extra_vars = playbook.extra_vars
        print(f"[DEBUG] Extra vars: {extra_vars}")  # Print all extra vars for inspection

        if 'awx_user_id' in extra_vars:
            user_id = extra_vars['awx_user_id']
        if 'awx_user_email' in extra_vars:
            user_email = extra_vars['awx_user_email']

        # Final debug print to confirm variables
        print(f"[DEBUG] Final user_id: {user_id}, user_email: {user_email}")
        return user_id or "unknown-user", user_email or "unknown-email"
