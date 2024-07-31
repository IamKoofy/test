pipeline {
    agent { label 'docker-aws' }
    
    parameters {
        choice(name: 'INSTANCE_NAME', choices: ['instance1', 'instance2', 'instance3'], description: 'Select EC2 Instance', multiSelectDelimiter: ',')
        choice(name: 'EC2_ACTION', choices: ['start', 'stop'], description: 'Choose EC2 Action')
        string(name: 'AWS_ACCOUNT', defaultValue: '', description: 'AWS Account ID')
        string(name: 'APP_NAME', defaultValue: '', description: 'Application Name')
        string(name: 'AWS_ENVIRONMENT', defaultValue: '', description: 'AWS Environment (DEV/QA/PROD)')
    }
    
    triggers {
        cron('H 0 * * 1') // Start instances at 12AM MST every Monday
        cron('H 19 * * 5') // Stop instances at 7PM MST every Friday
    }
    
    stages {
        stage('Set Build Name') {
            steps {
                script {
                    currentBuild.displayName = "#${env.BUILD_NUMBER} - ${params.EC2_ACTION} - ${params.APP_NAME} - ${params.AWS_ENVIRONMENT}"
                }
            }
        }
        
        stage('Stop EC2 - Selected') {
            when {
                expression { params.EC2_ACTION == 'stop' }
            }
            steps {
                withAWS(roleAccount: "${params.AWS_ACCOUNT}", role: 'JenkinsCrossAccountRole', region: 'us-east-1') {
                    script {
                        sh "aws ec2 describe-instances --filters Name=tag:'Name',Values='${params.INSTANCE_NAME}' --output text --query 'Reservations[*].Instances[*].InstanceId' > ./instance_id.list"
                        def instanceList = readFile(file: './instance_id.list').readLines()
                        echo "InstanceList: ${instanceList}"
                        
                        for (instanceID in instanceList) {
                            def instanceState = sh(script: "aws ec2 describe-instances --instance-id ${instanceID} --output text --query 'Reservations[*].Instances[*].State.Name'", returnStdout: true).trim()
                            if (instanceState == "running") {
                                echo "Stopping Instance: ${instanceID}"
                                sh "aws ec2 stop-instances --instance-ids ${instanceID}"
                            } else {
                                echo "Instance ${instanceID} is already stopped"
                            }
                        }
                    }
                }
            }
        }
        
        stage('Start EC2 - Selected') {
            when {
                expression { params.EC2_ACTION == 'start' }
            }
            steps {
                withAWS(roleAccount: "${params.AWS_ACCOUNT}", role: 'JenkinsCrossAccountRole', region: 'us-east-1') {
                    script {
                        sh "aws ec2 describe-instances --filters Name=tag:'Name',Values='${params.INSTANCE_NAME}' --output text --query 'Reservations[*].Instances[*].InstanceId' > ./instance_id.list"
                        def instanceList = readFile(file: './instance_id.list').readLines()
                        echo "InstanceList: ${instanceList}"
                        
                        for (instanceID in instanceList) {
                            def instanceState = sh(script: "aws ec2 describe-instances --instance-id ${instanceID} --output text --query 'Reservations[*].Instances[*].State.Name'", returnStdout: true).trim()
                            if (instanceState == "stopped") {
                                echo "Starting Instance: ${instanceID}"
                                sh "aws ec2 start-instances --instance-ids ${instanceID}"
                            } else {
                                echo "Instance ${instanceID} is already running"
                            }
                        }
                    }
                }
            }
        }
    }
    
    post {
        failure {
            script {
                echo "Sending Email Notification"
                mail(to: 'GTRTech@amexgbt.com',
                     subject: "FAILURE: ${params.APP_NAME} :: EC2 Instance :: ${params.EC2_ACTION} FAILED",
                     body: """
                    Team,

                    Please check the below FAILED console output link to view logs.
                    ${env.BUILD_URL}

                    ###################### EC2 Action Details ######################
                    * Timestamp: ${env.BUILD_TIMESTAMP}
                    * Application: ${params.APP_NAME}
                    * Account: ${params.AWS_ACCOUNT}
                    * Environment: ${params.AWS_ENVIRONMENT}
                    * Instance: ${params.INSTANCE_NAME}
                    * EC2Action: ${params.EC2_ACTION}
                    #############################################################

                    In case of any queries, write an email to the Team.

                    Regards,
                    DevOps Team
                    """)
            }
        }
    }
}
