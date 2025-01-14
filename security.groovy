param(
    [Parameter(Mandatory=$True)] [string] $serviceName,
    [Parameter(Mandatory=$True)] [string] $serviceFolder,
    [Parameter(Mandatory=$True)] [string] $artifactFolder,
    [Parameter(Mandatory=$False)] [string] $serviceUsername,
    [Parameter(Mandatory=$False)] [string] $servicePassword
)

# Check if artifact folder exists
if (!(Test-Path($artifactFolder))) {
    Write-Error "No artifact folder exists at $artifactFolder"
    Exit 1
}

# Define the path to the service DLL
$serviceDll = $serviceFolder + "\" + $serviceName + ".dll"

# Check if service folder exists
if (Test-Path($serviceFolder)) {
    Write-Host "Service folder already exists, checking to stop and remove the service"
    
    # Stop and remove the service if it exists
    $service = Get-Service $serviceName -ErrorAction SilentlyContinue
    if ($service) {
        Write-Host "Service exists: $service.Status"
        $serviceStates = "Running", "Paused"
        if ($serviceStates -contains $service.Status) {
            Write-Host "Stopping service"
            Stop-Service $serviceName
        }
        
        Write-Host "Removing service"
        sc.exe delete $serviceName
    }
    
    # Stop any running processes related to the service DLL
    Write-Host "Stopping any existing processes"
    Get-Process | ForEach-Object {
        $processVar = $_
        $_.Modules | ForEach-Object {
            if ($_.FileName -eq $serviceDll) {
                Stop-Process $processVar.Id -Force
            }
        }
    }

    # Remove locks on files
    Get-ChildItem $serviceFolder -Recurse | Unblock-File
    # Remove existing service files
    Write-Host "Removing existing service files"
    Remove-Item $serviceFolder\* -Recurse
} else {
    Write-Host "Creating service folder"
    New-Item $serviceFolder -ItemType Directory -Force
}

# Copy the service files from the artifact folder to the service folder
Write-Host "Copying service from artifact folder to service folder"
Copy-Item $artifactFolder\* -Destination $serviceFolder\ -Recurse

# Check if .NET Core is required to run the service
if (!(Test-Path $serviceDll)) {
    Write-Error "Service DLL not found at $serviceDll"
    Exit 1
}

# Install the service using the .exe directly
Write-Host "Installing service"

# Install service without NSSM, using New-Service
$exePath = "C:\Program Files\dotnet\dotnet.exe"
$serviceArgs = "$serviceDll"

if (![string]::IsNullOrWhiteSpace($serviceUsername) -and $serviceUsername -ne "?") {
    Write-Host "Setting service to run as user"
    # Create service using specific user credentials
    New-Service -Name $serviceName -Binary $exePath -ArgumentList $serviceArgs -Credential (New-Object System.Management.Automation.PSCredential($serviceUsername, (ConvertTo-SecureString $servicePassword -AsPlainText -Force))) -StartupType Automatic
} else {
    # Create service without any user credentials
    New-Service -Name $serviceName -Binary $exePath -ArgumentList $serviceArgs -StartupType Automatic
}

# Set service to delayed start (if needed)
Write-Host "Setting service as delayed start"
sc.exe config $serviceName start= delayed-auto

# Set service description
$descr = "HRG - " + $serviceName
sc.exe description $serviceName $descr

Write-Host "Service is installed and configured."

# Start the service
Start-Service $serviceName
Write-Host "Service started successfully."
