pipeline {
    agent any
    stages {
        stage('Read Config File') {
            steps {
                script {
                    def configFile = configFileProvider([configFile(fileId: 'my-config-file', variable: 'CONFIG_FILE')])
                    echo "Config file path: ${configFile}"
                }
            }
        }
    }
}
stages {
        stage('Get Version Info') {
            steps {
                script {
                    // Get git commit SHA
                    git commit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    // Get build number (assuming Jenkins environment variable)
                    buildNumber = env.BUILD_NUMBER

                    // Combine information and format (adjust as needed)
                    version = "${commit}-${buildNumber}"

                    // Store version in encrypted credential
                    withCredentials([usernamePassword(credentialsId: 'version', passwordVariable: 'VERSION')]) {
                        sh "echo ${version} |  openssl enc -aes-256-cbc -k \$VERSION -a"
                    }
                }
            }
        }
    }
}


pipeline {
    agent any

    stages {
        stage('Read Version') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'version', passwordVariable: 'VERSION')]) {
                        // Decrypt and store version
                        version = sh(script: 'echo \$VERSION | openssl dec -aes-256-cbc -k \$VERSION', returnStdout: true).trim()
                        echo "Version: ${version}"
                    }
                }
            }
        }
    }
}
