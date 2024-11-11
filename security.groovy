import json
import os
import requests
from datetime import datetime
from ansible.plugins.callback import CallbackBase

class CallbackModule(CallbackBase):
    CALLBACK_VERSION = 2.0
    CALLBACK_TYPE = 'notification'
    CALLBACK_NAME = 'log_to_cribl'

    def __init__(self):
        super(CallbackModule, self).__init__()
        
        # Cribl endpoint and authentication token
        self.cribl_endpoint = "https://default.main.vibrant-90msg.cribl.cloud:10070"
        self.auth_token = "9999anf-annnnd-9444k-998885859004"
        self.headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {self.auth_token}'
        }
        
        # Initialize user and template details
        self.user_full_name = "unknown-user"
        self.template_name = "unknown-template"
        self.job_id = self.get_job_id_from_hostname()
        self.task_results = []  # List to store task results

        # AWX API configuration
        self.api_base_url = "https://your-awx-instance/api/v2/"
        self.awx_api_token = "your_awx_api_token_here"
        self.awx_headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {self.awx_api_token}'
        }

    def get_job_id_from_hostname(self):
        """Extracts job_id from the container hostname."""
        hostname = os.getenv("HOSTNAME", "")
        parts = hostname.split("-")
        return parts[2] if len(parts) > 2 else None

    def fetch_awx_job_details(self):
        """Fetches job details from AWX API using job_id."""
        if not self.job_id:
            print("[DEBUG] Job ID could not be determined from hostname.")
            return

        url = f"{self.api_base_url}jobs/{self.job_id}/"
        response = requests.get(url, headers=self.awx_headers, verify=False)
        
        if response.status_code == 200:
            job_data = response.json()
            
            # Extract user's full name
            first_name = job_data.get("summary_fields", {}).get("created_by", {}).get("first_name", "")
            last_name = job_data.get("summary_fields", {}).get("created_by", {}).get("last_name", "")
            self.user_full_name = f"{first_name} {last_name}".strip() or "unknown-user"
            print(f"[DEBUG] Retrieved user_full_name: {self.user_full_name}")
            
            # Extract template name
            self.template_name = job_data.get("summary_fields", {}).get("job_template", {}).get("name", "unknown-template")
            print(f"[DEBUG] Template name: {self.template_name}")

        else:
            print(f"[ERROR] Failed to fetch job details: {response.status_code} - {response.text}")

    def v2_playbook_on_start(self, playbook):
        """Called at the start of a playbook run."""
        self.fetch_awx_job_details()

    def v2_runner_on_ok(self, result):
        """Records successful task results."""
        task_name = result._task.get_name()
        self.task_results.append({"task_name": task_name, "status": "OK"})

    def v2_runner_on_failed(self, result, ignore_errors=False):
        """Records failed task results."""
        task_name = result._task.get_name()
        self.task_results.append({"task_name": task_name, "status": "Failed"})

    def v2_runner_on_skipped(self, result):
        """Records skipped task results."""
        task_name = result._task.get_name()
        self.task_results.append({"task_name": task_name, "status": "Skipped"})

    def v2_playbook_on_stats(self, stats):
        """Called at the end of a playbook run to log stats and summary."""
        # Generate task summary from collected results
        task_summary = [{"name": t["task_name"], "status": t["status"]} for t in self.task_results]
        
        log_entry = {
            "job_id": self.job_id,
            "user_full_name": self.user_full_name,
            "template_name": self.template_name,
            "timestamp": datetime.utcnow().isoformat(),
            "task_summary": task_summary  # Include detailed task results
        }
        self.send_to_cribl(log_entry)

    def send_to_cribl(self, log_data):
        """Sends data to Cribl."""
        print(f"[DEBUG] Sending log data to Cribl: {log_data}")
        try:
            response = requests.post(self.cribl_endpoint, headers=self.headers, json=log_data, verify=False)
            response.raise_for_status()
            print(f"[DEBUG] Response from Cribl: {response.status_code} - {response.text}")
        except requests.exceptions.HTTPError as e:
            print(f"[ERROR] HTTP error occurred: {str(e)}")
        except requests.exceptions.RequestException as e:
            print(f"[ERROR] Request error: {str(e)}")
