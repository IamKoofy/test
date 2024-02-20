pipeline {
    agent any

    stages {
        stage('Build and Update Version') {
            steps {
                script {
                    // Define the version as GIT+build_number
                    def version = "${env.GIT_COMMIT}+${env.BUILD_NUMBER}"
                    
                    // Construct the path to the pom.xml file
                    def pomPath = "${env.workspace}/${env.buildDir}/pom.xml"

                    // Update the version in the pom.xml file
                    sh "xmlstarlet ed -L -u '//artifactID[text()=\"${param.COMPONENT}\"]/following-sibling::version[1]/text()' -v '${version}' ${pomPath}"
                }
            }
        }
    }
}
