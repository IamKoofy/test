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
        self.cribl_endpoint = "http://default.main.vibrant-90msg.cribl.cloud:10070"
        self.auth_token = "9999anf-annnnd-9444k-998885859004"
        self.headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {self.auth_token}'
        }
        self.user = None
        self.template_name = None

    def v2_playbook_on_start(self, playbook):
        # Capture user and template name information
        self.user = self._get_user()
        self.template_name = playbook._file_name
        log_entry = {
            "user": self.user,
            "template_name": self.template_name,
            "event_type": "playbook_start",
            "timestamp": datetime.utcnow().isoformat()
        }
        self.send_to_cribl(log_entry)

    def v2_playbook_on_stats(self, stats):
        # Capture the stats at the end of the playbook run
        summary = {host: stats.summarize(host) for host in stats.processed}
        log_entry = {
            "user": self.user,
            "template_name": self.template_name,
            "event_type": "playbook_end",
            "timestamp": datetime.utcnow().isoformat(),
            "summary": summary
        }
        self.send_to_cribl(log_entry)

    def send_to_cribl(self, log_data):
        # Send the log entry to Cribl
        try:
            response = requests.post(self.cribl_endpoint, headers=self.headers, json=log_data)
            if response.status_code != 200:
                print(f"Failed to send log to Cribl: {response.text}")
        except Exception as e:
            print(f"Error while sending log to Cribl: {str(e)}")

    def _get_user(self):
        # Get the LDAP user executing the template from the environment variables (AAP sets this)
        return os.getenv("AWX_USER_NAME", "unknown-user")
