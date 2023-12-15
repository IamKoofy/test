// vars/stopStartEC2.groovy

def call(Map params) {
    def AWS_ACCOUNT = params.AWS_ACCOUNT ?: 'default_value'
    def EC2_ACTION = params.EC2_ACTION ?: 'default_value'
    def INSTANCE_NAME = params.INSTANCE_NAME ?: 'default_value'
    def SLACK_CHANNEL = params.SLACK_CHANNEL ?: 'default_value'
    def AWS_ENVIRONMENT = params.AWS_ENVIRONMENT ?: 'default_value'

    stageStopStartEC2(AWS_ACCOUNT, EC2_ACTION, INSTANCE_NAME, SLACK_CHANNEL, AWS_ENVIRONMENT)
}

def stageStopStartEC2(String AWS_ACCOUNT, String EC2_ACTION, String INSTANCE_NAME, String SLACK_CHANNEL, String AWS_ENVIRONMENT) {
    stage('Stop Start EC2') {
        script {
            currentBuild.displayName = "#${env.BUILD_NUMBER} - ${EC2_ACTION} - ${APP_NAME} - ${AWS_ENVIRONMENT}"

            // Your existing pipeline logic goes here

            // For example:
            echo "AWS_ACCOUNT: ${AWS_ACCOUNT}"
            echo "EC2_ACTION: ${EC2_ACTION}"
            echo "INSTANCE_NAME: ${INSTANCE_NAME}"
            echo "SLACK_CHANNEL: ${SLACK_CHANNEL}"
            echo "AWS_ENVIRONMENT: ${AWS_ENVIRONMENT}"

            // Download instance_id.list from the resources directory
            def instanceList = readFile(file: "${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/resources/instance_id.list").readLines()

            // Check if the instanceList is empty
            if (instanceList.isEmpty()) {
                error("No instances found with the specified filters. Aborting the pipeline.")
            }

            echo "InstanceList : ${instanceList}"

            for (instanceID in instanceList) {
                instanceName = sh(script: "aws ec2 describe-instances --instance-id ${instanceID} --output text --query 'Reservations[*].Instances[*].{Instance_Name:Tags[?Key==`Name`]|[0].Value}'", returnStdout: true).trim()
                echo "*** Instance Name of ${instanceID} is ${instanceName} ***"

                instanceState = sh(script: "aws ec2 describe-instances --instance-id ${instanceID} --output text --query 'Reservations[*].Instances[*].State.Name'", returnStdout: true).trim()
                echo "*** Instance State of ${instanceName} / ${instanceID} is ${instanceState} ***"

                if (instanceState == "running") {
                    try {
                        timeout(time: 1, unit: 'MINUTES') {
                            IS_APPROVED = input(
                                message: "Are you OK to ${EC2_ACTION} the instance ${instanceName} / ${instanceID}?",
                                parameters: [
                                    [$class: 'ChoiceParameterDefinition',
                                        choices: ['YES', 'NO'].join('\n'),
                                        name: 'Select YES / NO']
                                ]
                            )
                        }
                    } catch (exc) {
                        wrap([$class: 'BuildUser']) {
                            error("${BUILD_USER} aborted the approval input to ${EC2_ACTION} the instance")
                        }
                    }

                    if ("${IS_APPROVED}" == "YES") {
                        echo "*** Instance ${instanceName} / ${instanceID} is going to be stopped ***"
                        sh "aws ec2 ${EC2_ACTION}-instances --instance-ids ${instanceID}"
                        def ec2Environment = AWS_ENVIRONMENT.toUpperCase();
                        echo "*** EC2 Environment is ${ec2Environment} ***"
                        wrap([$class: 'BuildUser']) {
                            echo "*** Sending Slack Notification ***"
                            slackSend channel: "${SLACK_CHANNEL}", color: "#FF7F50", message: "${BUILD_USER} :: ${APP_NAME} :: ${ec2Environment} :: EC2 Instance ${instanceName} / ${instanceID} is Stopped:: ${BUILD_TIMESTAMP}"
                        }
                    }
                } else {
                    echo "*** Instance ${instanceName} / ${instanceID} is already in stopped state, so no further action is required ***"
                }
            }
        }
    }
}
