pipeline {
    agent any

    stages {
        stage('Build and Update Version') {
            steps {
                script {
                    // Define the version as GIT+build_number
                    def version = "${env.GIT_COMMIT}+${env.BUILD_NUMBER}"
                    
                    // Update the version in the pom.xml file
                    sh "sed -i 's/<artifactID>${param.COMPONENT}<\\/artifactID>/<artifactID>${param.COMPONENT}<\\/artifactID>\\n<version>${version}<\\/version>/' /path/to/pom.xml"
                }
            }
        }
    }
}
