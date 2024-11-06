def _get_awx_user_info(self, playbook):
    """
    Attempt to retrieve user_id and user_email using awx_meta_vars or other playbook variables.
    """
    user_id = None
    user_email = None

    # Check all variables to see if `awx_user_id` or `awx_user_email` is present
    try:
        # Explore all variables in case `awx_user_id` or `awx_user_email` exists under a different name or structure
        vars_manager = self._play.get_variable_manager()
        all_vars = vars_manager.get_vars(play=playbook)
        
        # Print all vars for inspection to locate awx metadata
        print(f"[DEBUG] All vars available in playbook: {all_vars}")

        # Search for `awx_user_id` and `awx_user_email` directly if they exist
        user_id = all_vars.get('awx_user_id')
        user_email = all_vars.get('awx_user_email')
        
    except AttributeError as e:
        print(f"[ERROR] Failed to access all variables: {str(e)}")
        
    print(f"[DEBUG] Retrieved user_id: {user_id}, user_email: {user_email}")
    return user_id or "unknown-user", user_email or "unknown-email"
