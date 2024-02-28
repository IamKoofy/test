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
pipeline {
    agent any
    environment {
        VERSION = ''
    }
    stages {
        stage('Read from Managed File') {
            steps {
                script {
                    // Use configFileProvider to read the version information from the managed file
                    configFileProvider([configFile(fileId: 'my-version-file', variable: 'VERSION_FILE')]) {
                        // Inside the block, the content of the managed file is available as an environment variable
                        VERSION = sh(script: "cat ${VERSION_FILE}", returnStdout: true).trim()
                    }
                }
            }
        }
        stage('Use Version') {
            steps {
                echo "Version: ${VERSION}"
                // Use the version information in your pipeline
            }
        }
    }
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
