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

withCredentials([string(credentialsId: 'version', variable: 'VERSION')]) {
                        credentialsStore.updateSecretText(credentialsId: 'version', newDescription: 'Version Information', newSecret: version)
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
    environment {
        VERSION = ''
    }
    stages {
        stage('Set Version') {
            steps {
                script {
                    // Set the version information
                    VERSION = '1.0.0' // This can be your git commit + build number
                }
            }
        }
        stage('Write to Managed File') {
            steps {
                script {
                    // Use configFileProvider to read the version information from the managed file
                    configFileProvider([configFile(fileId: 'my-version-file', variable: 'VERSION_FILE')]) {
                        // Inside the block, the content of the managed file is available as an environment variable
                        def newVersionInfo = "${VERSION_FILE}".replaceFirst(/version:\s*\d+\.\d+\.\d+/, "version: ${VERSION}")
                        sh "echo '${newVersionInfo}' > ${VERSION_FILE}"
                    }
                }
            }
        }
    }
}
 store secret text 'version'  description='Version Information'"
