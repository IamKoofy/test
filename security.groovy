# Directly referencing pipeline variables
$serviceName = "$(ComponentName)"
$serviceFolder = "$(ServiceRootFolder)\$(ComponentName)"
$artifactFolder = "$(System.DefaultWorkingDirectory)\_$(ComponentName)-Build\$(ComponentName)"
$serviceUsername = "$(ServiceUser)"
$servicePassword = "$(ServicePassword)"

if (!(Test-Path $artifactFolder)) {
    Write-Error "No artifact folder exists at $artifactFolder"
    Exit 1
}

$serviceDll = "$serviceFolder\$serviceName.dll"

if (Test-Path $serviceFolder) {
    Write-Host "Service folder already exists, checking to stop and remove the service"
    
    $service = Get-Service $serviceName -ErrorAction SilentlyContinue
    if ($service) {
        Write-Host "Service exists."
        Write-Host $service.Status
        $serviceStates = "Running", "Paused"
        if ($serviceStates -contains $service.Status) {
            Write-Host "Stopping service"
            Stop-Service $serviceName
        }
    }

    if (Test-Path $serviceDll) {
        Write-Host "Uninstalling service"
        sc.exe delete $serviceName
    }

    Write-Host "Stopping any existing processes"
    Get-Process | foreach {
        $processVar = $_
        $_.Modules | foreach {
            if ($_.FileName -eq $serviceDll) {
                Stop-Process $processVar.id -Force
            }
        }
    }

    # Remove any locks on the files
    Get-ChildItem $serviceFolder -Recurse | Unblock-File

    # Remove all the items in the folder
    Write-Host "Removing existing service files"
    Remove-Item "$serviceFolder\*" -Recurse
} else {
    Write-Host "Creating $serviceFolder"
    New-Item $serviceFolder -ItemType Directory -Force
}

# Copy files across from artifactFolder
Write-Host "Copying service from artifactFolder to serviceFolder"
Copy-Item "$artifactFolder\*" -Destination $serviceFolder -Recurse

Write-Host "Installing service"
Start-Process "dotnet" -ArgumentList "$serviceDll"

if (![string]::IsNullOrWhiteSpace($serviceUsername) -and $serviceUsername -ne "?") {
    Write-Host "Setting service as user"
    sc.exe config $serviceName obj=$serviceUsername password=$servicePassword
}

Write-Host "Setting service as delayed start"
sc.exe config $serviceName start=delayed-auto

Write-Host "Setting service as delayed restart"
sc.exe config $serviceName AppRestartDelay=5000

$descr = "HRG - $serviceName"
sc.exe description $serviceName "$descr"

Write-Host "Service is installed"
