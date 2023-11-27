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
    script {
        echo "***Snyk Install***"

        withCredentials([string(credentialsId: 'SNYK_TOKEN', variable: 'SNYK_TOKEN')]) {
            sh 'npm install -g snyk --unsafe-perm'
            sh 'snyk auth ${SNYK_TOKEN}'
        }

        echo "***Snyk to HTML install***" 
        sh 'npm install -g snyk-to-html'

        echo "***Snyk Code Test***"
        catchError(buildResult: "UNSTABLE", stageResult: 'FAILURE') {
            sh 'snyk config set org="${SNYK_ORG_NAME}"'
            sh 'snyk code test --report --project-name="${projectName}" --json-file-output=code-results.json || true'
            sh 'snyk code test --severity-threshold=high'
        }

        echo "***Snyk to HTML - code***"
        sh 'snyk-to-html -i code-results.json -o code-results.html'

        echo "***Publish Code Artifact***"
        publishHTML(target: [allowMissing: false,
                             alwaysLinkToLastBuild: true,
                             keepAll: false,
                             reportDir: '.',
                             includes: '**/*',
                             reportFiles: 'code-results.html',
                             reportName: 'Snyk Code',
                             reportTitles: 'Snyk Code'])
    }
}

def nexusIQScan() {
    // Your existing NexusIQ scan code here
}

def sonarQubeScan() {
    // Your existing SonarQube scan code here
}
