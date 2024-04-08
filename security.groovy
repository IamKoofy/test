git clone "{{ github_repo_url }}/non-cde-dev" /tmp/clone_repo

# Copy the directory or files to the repository
cp -r "/tmp/{{ namespace }}_backup_{{ ansible_date_time.iso8601_basic }}" /tmp/clone_repo

# Commit the changes
cd /tmp/clone_repo
