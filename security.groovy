pipeline {
    agent { label 'jenkins-openshift-v5' }

    parameters {
        choice(name: 'JAVA_VERSION', choices: ['java17', 'java18', 'java21'], description: 'Select the Java Version')
        string(name: 'BUILD_VERSION', defaultValue: '1.0.${BUILD_NUMBER}', description: 'Version of the Build')
        string(name: 'ARTIFACT_ID', defaultValue: 'Secret_Component', description: 'Artifact ID')
        string(name: 'GROUP_ID', defaultValue: 'org.test', description: 'Group ID')
    }

    environment {
        NEXUS_URL = 'http://nexus.yourdomain.com'  // Replace with actual Nexus URL
        NEXUS_REPO = 'maven-test'
        REPO_FOLDER = "${params.JAVA_VERSION}"
        ARTIFACT_PATH = "secret_component/org/test/${params.JAVA_VERSION}"
    }

    stages {
        stage('Git Checkout') {
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
                    sh "sed -i 's/<artifactId>.*</<artifactId>${params.ARTIFACT_ID}</g' Secret_Component_Java/pom.xml"
                    sh "sed -i 's/<groupId>.*</<groupId>${params.GROUP_ID}</g' Secret_Component_Java/pom.xml"
                    sh "sed -i 's/<version>.*</<version>${params.BUILD_VERSION}</g' Secret_Component_Java/pom.xml"
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

        stage('Publish to Nexus') {
            steps {
                dir("Secret_Component_Java") {
                    script {
                        echo "*** Publishing to Nexus Repository ***"
                        nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: "${env.NEXUS_REPO}", packages: [
                            [$class: 'MavenPackage', mavenAssetList: [
                                [classifier: '', extension: 'jar', filePath: "target/${params.ARTIFACT_ID}-${params.BUILD_VERSION}.jar"]
                            ], mavenCoordinate: [artifactId: "${params.ARTIFACT_ID}", groupId: "${params.GROUP_ID}", packaging: 'jar', version: "${params.BUILD_VERSION}"]]
                        ]
                    }
                }
            }
        }

        stage('Move Artifact to Folder') {
            steps {
                script {
                    echo "*** Moving artifact to ${ARTIFACT_PATH} ***"
                    sh "curl -u username:password --upload-file target/${params.ARTIFACT_ID}-${params.BUILD_VERSION}.jar ${NEXUS_URL}/repository/${NEXUS_REPO}/${ARTIFACT_PATH}/"
                }
            }
        }
    }
}
