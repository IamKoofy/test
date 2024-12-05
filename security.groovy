pipeline {
    agent any
    environment {
        DOCKER_IMAGE = "dotnet/core/sdk-3.1:v3.1"
    }
    stages {
        stage("Pull Docker Image") {
            steps {
                script {
                    docker.withRegistry("https://dockerregistry.com:9444", "NEXUS-REPO-CREDENTIALS") {
                        def image = docker.image(DOCKER_IMAGE)
                        image.pull()
                        echo "Docker image ${DOCKER_IMAGE} pulled successfully."
                    }
                }
            }
        }
        stage("Policy Evaluation") {
            steps {
                script {
                    nexusPolicyEvaluation(
                        enableDebugLogging: false,
                        failBuildOnNetworkError: false,
                        iqApplication: "my_application", // Adjust to your Nexus IQ application
                        iqScanPatterns: [
                            [scanPattern: "**/my_application.war"],
                            [scanPattern: "**/${DOCKER_IMAGE}"]
                        ],
                        iqStage: "build"
                    )
                }
            }
        }
    }
    post {
        always {
            echo "Pipeline execution completed."
        }
    }
}
