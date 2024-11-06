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
        self.user = None
        self.template_name = None

    def v2_playbook_on_start(self, playbook):
        self.user = self._get_user_from_api(playbook)
        self.template_name = playbook._file_name
        log_entry = {
            "user": self.user,
            "template_name": self.template_name,
            "event_type": "playbook_start",
            "timestamp": datetime.utcnow().isoformat()
        }
        self.send_to_cribl(log_entry)

    def v2_playbook_on_stats(self, stats):
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
        try:
            response = requests.post(self.cribl_endpoint, headers=self.headers, json=log_data, verify=False)
            response.raise_for_status()  # Raise an error for bad responses
        except requests.exceptions.HTTPError as e:
            print(f"HTTP error occurred: {str(e)}")  # This will give more context on the error
        except requests.exceptions.RequestException as e:
            print(f"Request error: {str(e)}")

    def _get_user_from_api(self, playbook):
        # Extract job ID and make API call to fetch user info
        job_id = playbook._job_id  # Assuming playbook has _job_id attribute
        job_url = f"http://localhost/api/v2/jobs/{job_id}/"  # Adjust API URL as needed
        response = requests.get(job_url, headers=self.headers)
        if response.status_code == 200:
            job_data = response.json()
            user = job_data.get('created_by', {}).get('username', 'unknown-user')
            return user
        else:
            print(f"Failed to fetch job data: {response.text}")
            return "unknown-user"
