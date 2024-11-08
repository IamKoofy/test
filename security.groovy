def v2_playbook_on_start(self, playbook):
    # Retrieve all playbook variables for debugging
    play_vars = playbook.__dict__.get('extra_vars', {})
    print(f"All Playbook Vars for Debugging: {play_vars}")
    
    # Retrieve specific variables
    self.user_name = play_vars.get("awx_user_name", "unknown-user")
    self.user_email = play_vars.get("awx_user_email", "unknown-email")
    self.template_name = play_vars.get("awx_template_name", "unknown-template")

    # Print for debugging
    print(f"User Name: {self.user_name}, User Email: {self.user_email}, Template Name: {self.template_name}")
    
    log_entry = {
        "user_name": self.user_name,
        "user_email": self.user_email,
        "template_name": self.template_name,
        "event_type": "playbook_start",
        "timestamp": datetime.utcnow().isoformat()
    }
    self.send_to_cribl(log_entry)
