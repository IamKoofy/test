---
- name: Monitor Windows Services
  hosts: windows
  gather_facts: no

  tasks:
    - name: Check Windows services
      win_shell: |
        $services = @(
            'queuereaderservice',  # Removed .exe
            'TestORG',  # Removed full path and .exe
            'QueueProcessorShell'  # Removed .exe
        )
        
        $serviceStatuses = @()  # Array to store service statuses

        foreach ($service in $services) {
            $status = Get-Process -Name $service -ErrorAction SilentlyContinue
            if ($status) {
                $serviceStatuses += "Service $service is running."
            } else {
                $serviceStatuses += "Service $service is NOT running."
            }
        }

        $serviceStatuses -join "`n"  # Output statuses line by line

        if ($serviceStatuses -match "NOT running") {
            $emailParams = @{
                From = "monitor@yourdomain.com"
                To = "yourDL@domain.com"
                Subject = "Service Not Running Alert"
                Body = "The following services have issues: `n $($serviceStatuses -join '`n')"
                SmtpServer = "smtp.yourdomain.com"
            }
            Send-MailMessage @emailParams
        }
      register: service_check_output

    - name: Debug service check output
      debug:
        var: service_check_output.stdout_lines  # Print output line by line
