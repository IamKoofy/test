import os
import requests

class CallbackModule(object):
    CALLBACK_VERSION = 2.0
    CALLBACK_TYPE = 'notification'
    CALLBACK_NAME = 'custom_callback'

    def __init__(self):
        # Obtain hostname environment variable
        self.hostname = os.getenv("HOSTNAME", "")
        # Extract the job ID from the hostname (assuming it's in the form: 'automation-job-<job_id>-<random_suffix>')
        self.job_id = self.extract_job_id_from_hostname(self.hostname)
        # API base URL and token
        self.api_base_url = "https://your-awx-instance/api/v2/"
        self.api_token = "your_api_token_here"  # Replace with your actual API token

    def extract_job_id_from_hostname(self, hostname):
        # Extracts job ID from hostname like "automation-job-9897-hyhq"
        parts = hostname.split("-")
        if len(parts) > 2:
            return parts[2]  # "9897" in "automation-job-9897-hyhq"
        return None

    def get_job_details(self):
        if not self.job_id:
            print("Job ID could not be determined from hostname.")
            return None

        url = f"{self.api_base_url}jobs/{self.job_id}/"
        headers = {
            "Authorization": f"Bearer {self.api_token}"
        }

        response = requests.get(url, headers=headers, verify=False)
        if response.status_code == 200:
            job_data = response.json()
            return {
                "template_name": job_data.get("name", "Unknown Template"),
                "launched_by": job_data.get("summary_fields", {}).get("created_by", {}).get("username", "Unknown User"),
            }
        else:
            print(f"Failed to fetch job details: {response.status_code} - {response.text}")
            return None

    def v2_playbook_on_start(self, playbook):
        # Fetch job details
        job_details = self.get_job_details()
        if job_details:
            template_name = job_details["template_name"]
            launched_by = job_details["launched_by"]
            # Print or log the details as needed for debugging or audit purposes
            print(f"Job Template Name: {template_name}")
            print(f"Launched By: {launched_by}")
