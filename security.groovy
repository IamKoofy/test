// vars/security.groovy

def call(String projectType, String projectName, String targetPath) {
    // Call the appropriate scan function based on projectType
    switch (projectType) {
        case 'dotnet':
            dotnetScan(projectName, targetPath)
            break
        case 'maven':
            mavenScan(projectName, targetPath)
            break
        case 'nodejs':
        case 'npm':
            nodejsScan(projectName, targetPath)
            break
        default:
            error "Unsupported project type: ${projectType}"
    }
}

def dotnetScan(String projectName, String targetPath) {
    echo "Performing dotnet scan for ${projectName} in ${targetPath}"
    // Call your dotnet-specific scan functions here
    snykScan(projectName, targetPath)
    nexusIQScan()
    sonarQubeScan()
}

def mavenScan(String projectName, String targetPath) {
    echo "Performing Maven scan for ${projectName} in ${targetPath}"
    // Call your Maven-specific scan functions here
    snykScan(projectName, targetPath)
    nexusIQScan()
    sonarQubeScan()
}

def nodejsScan(String projectName, String targetPath) {
    echo "Performing Node.js/NPM scan for ${projectName} in ${targetPath}"
    // Call your Node.js/NPM-specific scan functions here
    snykScan(projectName, targetPath)
    nexusIQScan()
    sonarQubeScan()
}

def snykScan(String projectName, String targetPath) {
    // Your existing Snyk scan code here
}

def nexusIQScan() {
    // Your existing NexusIQ scan code here
}

def sonarQubeScan() {
    // Your existing SonarQube scan code here
}
