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
            'Authorization': f'Bearer {self.auth_token}'
        }
        self.user_full_name = "unknown user"
        self.template_name = None
        self.job_id = None
        self.task_results = []  # To store each task’s name and status

    def v2_playbook_on_start(self, playbook):
        self.job_id = self._extract_job_id_from_hostname()
        self.user_full_name = self._get_user_full_name()
        self.template_name = self._get_template_name()
        print(f"[DEBUG] Job started - job_id: {self.job_id}, user_full_name: {self.user_full_name}, template_name: {self.template_name}")

    def v2_runner_on_ok(self, result):
        task_name = result._task.get_name()
        self.task_results.append({"task_name": task_name, "status": "OK"})

    def v2_runner_on_failed(self, result, ignore_errors=False):
        task_name = result._task.get_name()
        self.task_results.append({"task_name": task_name, "status": "Failed"})

    def v2_runner_on_skipped(self, result):
        task_name = result._task.get_name()
        self.task_results.append({"task_name": task_name, "status": "Skipped"})

    def v2_playbook_on_stats(self, stats):
        # Generate task summary from collected results
        task_summary = [{"name": t["task_name"], "status": t["status"]} for t in self.task_results]
        
        # Log final data to Cribl
        log_entry = {
            "job_id": self.job_id,
            "user_full_name": self.user_full_name,
            "template_name": self.template_name,
            "timestamp": datetime.utcnow().isoformat(),
            "task_summary": task_summary  # Include detailed task results
        }
        self.send_to_cribl(log_entry)

    def send_to_cribl(self, log_data):
        print(f"[DEBUG] Sending log data to Cribl: {log_data}")
        try:
            response = requests.post(self.cribl_endpoint, headers=self.headers, json=log_data, verify=False)
            response.raise_for_status()
            print(f"[DEBUG] Response from Cribl: {response.status_code} - {response.text}")
        except requests.exceptions.HTTPError as e:
            print(f"[ERROR] HTTP error occurred: {str(e)}")
        except requests.exceptions.RequestException as e:
            print(f"[ERROR] Request error: {str(e)}")

    def _extract_job_id_from_hostname(self):
        hostname = os.getenv("HOSTNAME", "")
        job_id = hostname.split("-")[2] if len(hostname.split("-")) > 2 else "unknown"
        print(f"[DEBUG] Extracted job_id: {job_id}")
        return job_id

    def _get_user_full_name(self):
        # Mock function to retrieve full name from API call
        return "John Doe"

    def _get_template_name(self):
        # Mock function to retrieve template name from API call
        return "Sample Template"
