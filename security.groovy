def v2_playbook_on_start(self, playbook):
        # Attempt to retrieve extra_vars from the playbook
        variable_manager = playbook.get_variable_manager()
        extra_vars = variable_manager._extra_vars if hasattr(variable_manager, '_extra_vars') else {}

        # Capture the job ID if available
        tower_job_id = extra_vars.get('tower_job_id', 'unknown-job-id')
        print("AWX Job ID:", tower_job_id)

        # Print all extra variables
        print("All Extra Vars Available:", extra_vars)
