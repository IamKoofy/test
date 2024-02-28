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
