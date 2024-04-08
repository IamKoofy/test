 cd "/tmp/{{ namespace }}_backup_{{ ansible_date_time.iso8601_basic }}"
    git init
    git config --global user.name "{{ git_username }}"
    git config --global user.email "{{ git_email }}"
    git add .
    git commit -m "Backup commit"
    git remote add origin "https://{{ github_repo_url }}/non-cde-dev/"
    git branch -m master main  # Rename master branch to main
    git push -u origin main    # Push changes to main branch
