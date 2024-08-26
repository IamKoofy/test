---
- name: Monitor Windows Services
  hosts: windows
  gather_facts: no

  tasks:
    - name: Check Windows services
      win_shell: |
        $services = @(
            'queuereaderservice.exe',
            'C:\CTE\.NET CORE\TestORG.exe',
            'QueueProcessorShell.exe'
        )
        
        $serviceStatuses = foreach ($service in $services) {
            $status = Get-Process -Name $service -ErrorAction SilentlyContinue
            if (-not $status) {
                $service
            }
        }

        if ($serviceStatuses.Count -gt 0) {
            $emailParams = @{
                From = "monitor@yourdomain.com"
                To = "yourDL@domain.com"
                Subject = "Service Not Running Alert"
                Body = "The following services are not running: $($serviceStatuses -join ', ')"
                SmtpServer = "smtp.yourdomain.com"
            }
            Send-MailMessage @emailParams
        }
      register: service_check_output

    - name: Debug service check output
      debug:
        var: service_check_output
