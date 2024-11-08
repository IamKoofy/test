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
            'Authorization': f'Authtoken {self.auth_token}'
        }
        self.user_name = None
        self.user_email = None
        self.template_name = None

    def v2_playbook_on_start(self, playbook):
        # Capture the AWX user and template info from extra vars
        play_vars = playbook._variable_manager._extra_vars  # Access extra vars
        self.user_name = play_vars.get("awx_user_name", "unknown-user")
        self.user_email = play_vars.get("awx_user_email", "unknown-email")
        self.template_name = play_vars.get("awx_template_name", "unknown-template")

        # Create log entry
        log_entry = {
            "user_name": self.user_name,
            "user_email": self.user_email,
            "template_name": self.template_name,
            "event_type": "playbook_start",
            "timestamp": datetime.utcnow().isoformat()
        }
        self.send_to_cribl(log_entry)

    def v2_playbook_on_stats(self, stats):
        summary = {host: stats.summarize(host) for host in stats.processed}
        log_entry = {
            "user_name": self.user_name,
            "user_email": self.user_email,
            "template_name": self.template_name,
            "event_type": "playbook_end",
            "timestamp": datetime.utcnow().isoformat(),
            "summary": summary
        }
        self.send_to_cribl(log_entry)

    def send_to_cribl(self, log_data):
        try:
            response = requests.post(self.cribl_endpoint, headers=self.headers, json=log_data, verify=False)
            if response.status_code != 200:
                print(f"Failed to send log to Cribl: {response.text}")
        except requests.exceptions.RequestException as e:
            print(f"Error while sending log to Cribl: {str(e)}")
