pipeline {
    agent {label 'jenkins-openshift-v5'}
    
    parameters {
        choice(name: 'JAVA_VERSION', choices: ['java17', 'java18', 'java21'], description: 'Select the Java Version')
    }

    environment {
        JAVA_FOLDER = "${params.JAVA_VERSION}" // Folder in Nexus based on selected version
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
                    sh "sed -i 's/<maven.compiler.source>.*</<maven.compiler.source>${params.JAVA_VERSION.replace('java', '')}</g' Secret_Component_Java/pom.xml"
                    sh "sed -i 's/<maven.compiler.target>.*</<maven.compiler.target>${params.JAVA_VERSION.replace('java', '')}</g' Secret_Component_Java/pom.xml"
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
                        def repoFolder = "${params.JAVA_VERSION}" // Push to java17, java18, or java21
                        nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: "maven-${repoFolder}", packages: [
                            [$class: 'MavenPackage', mavenAssetList: [
                                [classifier: '', extension: '', filePath: 'target/Secret_Component-1.0.3.jar']
                            ], mavenCoordinate: [artifactId: 'secret-component', groupId: 'secret_component.org.test', packaging: 'jar', version: '${BUILD_NUMBER}']]
                        ], tagName: 'ccoe-secret-component'
                    }
                }
            }
        }
    }
}
