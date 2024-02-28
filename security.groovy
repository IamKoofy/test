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

    withCredentials([string(credentialsId: 'version', variable: 'VERSION')]) {
                        withCredentials(id: 'version') {
                            credentialsStore.updateSecretText(credentialsId: 'version', newDescription: 'Version Information', newSecret: version)ION', returnStdout: true).trim()
                        echo "Version: ${version}"
                    }
                }
            }
        }
    }
}
sh "echo ${version} |  jenkins credentials store secret text 'version'  description='Version Information'"
