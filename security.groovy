---
- name: Monitor Azure DevOps Agent Disk Space
  hosts: windows_agents
  gather_facts: no
  tasks:
    - name: Check disk usage on D: drive
      win_shell: |
        $disk = Get-PSDrive -Name D
        $usedSpace = ($disk.Used / $disk.Used + $disk.Free) * 100
        Write-Output $usedSpace
      register: disk_usage

    - name: Send email alert if disk space exceeds 90%
      win_shell: |
        $smtpServer = "smtp.example.com"
        $from = "noreply@example.com"
        $to = "admin@example.com"
        $subject = "Warning: D: Drive Space Critical on {{ inventory_hostname }}"
        $body = "The D: drive on {{ inventory_hostname }} is over 90% full. Please take action."
        
        $message = New-Object System.Net.Mail.MailMessage
        $message.From = $from
        $message.To.Add($to)
        $message.Subject = $subject
        $message.Body = $body

        $smtp = New-Object Net.Mail.SmtpClient($smtpServer)
        $smtp.Send($message)
      when: disk_usage.stdout | float > 90
