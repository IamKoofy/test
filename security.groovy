def v2_playbook_on_start(self, playbook):
    # Access the job ID from ansible_env if available
    ansible_env = self._playbook._variable_manager._extra_vars.get('ansible_env', {})
    job_id = ansible_env.get('JOB_ID') or ansible_env.get('tower_job_id')
    
    print("Ansible Environment Variables:", ansible_env)
    print("JOB_ID:", job_id)
    print("All Available Extra Vars:", self._playbook._variable_manager._extra_vars)
