pipeline {
    agent { label 'jenkins-openshift-v5' }

    parameters {
        choice(name: 'JAVA_VERSION', choices: ['java17', 'java18', 'java21'], description: 'Select the Java Version')
    }

    environment {
        JAVA_VERSION_NUM = "${params.JAVA_VERSION.replace('java', '')}" // Numeric version for POM and Nexus path
    }

    stages {
        stage('GitCheckOut') {
            steps {
                echo "*** Checking out Code from GitHub ***"
                git branch: 'main', credentialsId: 'CCOE-GitHub-Service-Account', url: 'https://github.com/test/cco-java.git'
            }
        }

        stage('Update POM') {
            steps {
                script {
                    echo "*** Updating pom.xml with selected Java version ***"
                    sh "sed -i 's/<maven.compiler.source>.*</<maven.compiler.source>${JAVA_VERSION_NUM}</g' Secret_Component_Java/pom.xml"
                    sh "sed -i 's/<maven.compiler.target>.*</<maven.compiler.target>${JAVA_VERSION_NUM}</g' Secret_Component_Java/pom.xml"
                }
            }
        }

        stage('Maven Build') {
            steps {
                dir("Secret_Component_Java") {
                    script {
                        echo "*** Building Code using Maven ***"
                        sh 'mvn -X -f pom.xml clean install -Dmaven.test.skip=true'
                    }
                }
            }
        }

        stage('Sonar Scan') {
            steps {
                dir("Secret_Component_Java") {
                    script {
                        echo "*** Analysing Code using SonarQube Skipped ***"
                    }
                }
            }
        }

        stage('Publish to Nexus Repository Manager') {
            steps {
                dir("Secret_Component_Java") {
                    script {
                        echo "*** Publishing to Nexus Repository based on Java version ***"
                        def filePath = "target/Secret_Component-${BUILD_NUMBER}.jar" // Artifact file path
                        def mavenCoordinate = [artifactId: 'secret-component', groupId: 'secret_component.org.test', packaging: 'jar', version: "${BUILD_NUMBER}"]

                        // Define Nexus path based on Java version
                        def nexusPath = "java-${JAVA_VERSION_NUM}/secret-component/${BUILD_NUMBER}/"

                        nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'maven-test', packages: [
                            [$class: 'MavenPackage', mavenAssetList: [
                                [classifier: '', extension: 'jar', filePath: filePath]
                            ], mavenCoordinate: mavenCoordinate]
                        ], tagName: 'ccoe-secret-component'
                        echo "*** Successfully published ${filePath} to ${nexusPath} folder in Nexus Repository ***"
                    }
                }
            }
        }
    }
}
