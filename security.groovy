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

                    // Define the artifact ID for which you want to update the version
                    def artifactId = "TActions" // or use params.COMPONENT

                    // Update the version in the pom.xml file using sed
                    sh "sed -i '/<artifactId>${artifactId}<\\/artifactId>/{N;s/<version>1.0.0-SNAPSHOT<\\/version>/<version>${version}<\\/version>/}' ${pomPath}"
                }
            }
        }
    }
}
