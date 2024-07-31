pipeline {
    agent { label 'docker-aws' }
    
    environment {
        AWS_REGION = 'us-east-1' // Set the AWS region here
    }
    
    parameters {
        string(name: 'AWS_ACCOUNT', defaultValue: '', description: 'AWS Account ID')
        string(name: 'APP_NAME', defaultValue: '', description: 'Application Name')
        choice(name: 'EC2_ACTION', choices: ['start', 'stop'], description: 'Choose EC2 Action')
        string(name: 'AWS_ENVIRONMENT', defaultValue: '', description: 'AWS Environment (DEV/QA/PROD)')
        choice(name: 'INSTANCE_NAME', choices: ['instance1,instance2,instance3'], description: 'Select EC2 Instances (comma-separated)')
    }
    
    triggers {
        cron('0 0 * * 1') // Start instances at 12AM MST every Monday
        cron('0 19 * * 5') // Stop instances at 7PM MST every Friday
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
                withAWS(roleAccount: "${params.AWS_ACCOUNT}", role: 'JenkinsCrossAccountRole', region: "${env.AWS_REGION}") {
                    script {
                        def instances = params.INSTANCE_NAME.split(',')
                        instances.each { instance ->
                            def instanceID = sh(script: "aws ec2 describe-instances --filters Name=tag:Name,Values=${instance} --query 'Reservations[*].Instances[*].InstanceId' --output text", returnStdout: true).trim()
                            def instanceState = sh(script: "aws ec2 describe-instances --instance-ids ${instanceID} --query 'Reservations[*].Instances[*].State.Name' --output text", returnStdout: true).trim()
                            if (instanceState == "running") {
                                echo "Stopping Instance: ${instanceID} (${instance})"
                                sh "aws ec2 stop-instances --instance-ids ${instanceID}"
                            } else {
                                echo "Instance ${instanceID} (${instance}) is already stopped"
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
                withAWS(roleAccount: "${params.AWS_ACCOUNT}", role: 'JenkinsCrossAccountRole', region: "${env.AWS_REGION}") {
                    script {
                        def instances = params.INSTANCE_NAME.split(',')
                        instances.each { instance ->
                            def instanceID = sh(script: "aws ec2 describe-instances --filters Name=tag:Name,Values=${instance} --query 'Reservations[*].Instances[*].InstanceId' --output text", returnStdout: true).trim()
                            def instanceState = sh(script: "aws ec2 describe-instances --instance-ids ${instanceID} --query 'Reservations[*].Instances[*].State.Name' --output text", returnStdout: true).trim()
                            if (instanceState == "stopped") {
                                echo "Starting Instance: ${instanceID} (${instance})"
                                sh "aws ec2 start-instances --instance-ids ${instanceID}"
                            } else {
                                echo "Instance ${instanceID} (${instance}) is already running"
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
                    * Instances: ${params.INSTANCE_NAME}
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
