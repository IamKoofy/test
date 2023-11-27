// vars/security.groovy

def call(String projectType) {
    // Call the appropriate scan function based on projectType
    switch (projectType) {
        case 'dotnet':
            dotnetScan()
            break
        case 'maven':
            mavenScan()
            break
        case 'nodejs':
        case 'npm':
            nodejsScan()
            break
        default:
            error "Unsupported project type: ${projectType}"
    }
}

def dotnetScan() {
    echo "Performing dotnet scan"
    // Call your dotnet-specific scan functions here
    snykScan()
    nexusIQScan()
    sonarQubeScan()
}

def mavenScan() {
    echo "Performing Maven scan"
    // Call your Maven-specific scan functions here
    snykScan()
    nexusIQScan()
    sonarQubeScan()
}

def nodejsScan() {
    echo "Performing Node.js/NPM scan"
    // Call your Node.js/NPM-specific scan functions here
    snykScan()
    nexusIQScan()
    sonarQubeScan()
}

def snykScan() {
    script {
        echo "***Snyk Install***"

        withCredentials([string(credentialsId: 'SNYK_TOKEN', variable: 'SNYK_TOKEN'),
                        string(credentialsId: 'SNYK_ORG_NAME', variable: 'SNYK_ORG_NAME')]) {
            sh 'npm install -g snyk --unsafe-perm'
            sh 'snyk auth ${SNYK_TOKEN}'
        }

        echo "***Snyk to HTML install***" 
        sh 'npm install -g snyk-to-html'

        echo "***Snyk Code Test***"
        catchError(buildResult: "UNSTABLE", stageResult: 'FAILURE') {
            sh "snyk config set org=${SNYK_ORG_NAME}"
            sh 'snyk code test --report --json-file-output=code-results.json || true'
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
