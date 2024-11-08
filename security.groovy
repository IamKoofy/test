import json
import requests
from datetime import datetime
from ansible.plugins.callback import CallbackBase
import os
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
            'Authorization': f'Bearer {self.auth_token}'  # Ensure this is correct
        }
        self.user = None
        self.template_name = None

    def v2_playbook_on_start(self, playbook):
        self.user = self._get_user(playbook)
        self.template_name = self._get_template_name(playbook)
        
        # Debugging: Print the user and template name to the container logs
        logging.debug(f"Playbook started by user: {self.user}")
        logging.debug(f"Template name: {self.template_name}")
        
        # Send info to Cribl
        log_entry = {
            "user": self.user,
            "template_name": self.template_name,
            "event_type": "playbook_start",
            "timestamp": datetime.utcnow().isoformat()
        }
        self.send_to_cribl(log_entry)

    def v2_playbo
