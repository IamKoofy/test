pipeline {
    agent any

    parameters {
        string(name: 'component', defaultValue: 'actions', description: 'Component name')
    }

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    // Clone the repository
                    git branch: 'main', url: 'https://github.com/your-repository.git'
                }
            }
        }

        stage('Replace Version in pom.xml') {
            steps {
                script {
                    // Define the mapping of component names to version placeholders
                    def versionMap = [
                        'actions': '${env.actionsBuildversion}',
                        'transform': '${env.transformBuildversion}'
                        // Add more mappings as needed
                    ]

                    // Get the version placeholder based on the selected component
                    def versionPlaceholder = versionMap[params.component]

                    // Replace the placeholder with the actual version
                    sh "sed -i 's|<version>${versionPlaceholder}</version>|<version>${env.actionsBuildversion}</version>|' pom.xml"
                }
            }
        }

        stage('Build') {
            steps {
                // Build the project
                sh 'mvn clean package'
            }
        }
    }
}
