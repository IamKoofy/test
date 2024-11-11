import os
import json
import requests
from datetime import datetime
from ansible.plugins.callback import CallbackBase
import logging

# Set up logging for debugging purposes
logging.basicConfig(level=logging.DEBUG)

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
            'Authorization': f'Bearer {self.auth_token}'
        }
        self.user = "unknown-user"
        self.template_name = "unknown-template"
        self.job_id = os.getenv("JOB_ID")

    def v2_playbook_on_start(self, playbook):
        if self.job_id:
            self.user = self.get_user_from_awx(self.job_id)
        self.template_name = playbook._file_name if playbook._file_name else "unknown-template"

        # Debugging: Print the user, template name, and job ID
        logging.debug(f"Playbook started by user: {self.user}")
        logging.debug(f"Template name: {self.template_name}")
        logging.debug(f"Job ID: {self.job_id}")

        # Send info to Cribl
        log_entry = {
            "user": self.user,
            "template_name": self.template_name,
            "job_id": self.job_id,
            "event_type": "playbook_start",
            "timestamp": datetime.utcnow().isoformat()
        }
        self.send_to_cribl(log_entry)

    def v2_playbook_on_stats(self, stats):
        summary = {host: stats.summarize(host) for host in stats.processed}
        log_entry = {
            "user": self.user,
            "template_name": self.template_name,
            "job_id": self.job_id,
            "event_type": "playbook_end",
            "timestamp": datetime.utcnow().isoformat(),
            "summary": summary
        }
        self.send_to_cribl(log_entry)

    def get_user_from_awx(self, job_id):
        """Retrieve the username of the job initiator from the AWX API"""
        awx_api_url = f"https://tower.com/api/v2/jobs/{job_id}/"
        api_headers = {
            'Authorization': f"Bearer {self.auth_token}"
        }
        try:
            response = requests.get(awx_api_url, headers=api_headers, verify=False)
            response.raise_for_status()
            job_data = response.json()
            username = job_data.get("summary_fields", {}).get("created_by", {}).get("username", "unknown-user")
            logging.debug(f"User who launched the job: {username}")
            return username
        except requests.exceptions.HTTPError as e:
            logging.error(f"HTTP error occurred: {str(e)}")
        except requests.exceptions.RequestException as e:
            logging.error(f"Request error: {str(e)}")
        return "unknown-user"

    def send_to_cribl(self, log_data):
        """Send the log data to Cribl"""
        try:
            response = requests.post(self.cribl_endpoint, headers=self.headers, json=log_data, verify=False)
            response.raise_for_status()
        except requests.exceptions.HTTPError as e:
            logging.error(f"HTTP error occurred: {str(e)}")
        except requests.exceptions.RequestException as e:
            logging.error(f"Request error: {str(e)}")
