// vars/awsActions.groovy

def call(params) {
    setBuildName(params)
    performEc2Actions(params)
}

def setBuildName(params) {
    echo "Setting build name..."
    currentBuild.displayName = "#${env.BUILD_NUMBER} - ${params.EC2_ACTION} - ${params.APP_NAME} - ${params.AWS_ENVIRONMENT}"
}

def performEc2Actions(params) {
    if (params.EC2_ALL_INSTANCES == 'yes') {
        performEc2ActionsAllInstances(params)
    } else {
        performEc2ActionsSelectedInstances(params)
    }
}

def performEc2ActionsAllInstances(params) {
    echo "Performing EC2 actions on all instances..."
    // Implement logic for all instances
}

def performEc2ActionsSelectedInstances(params) {
    echo "Performing EC2 actions on selected instances..."
    withAWS(credentials: "myteam-${params.AWS_ACCOUNT}-${params.AWS_ENVIRONMENT}", region: 'us-east-1') {
        def instanceList = getEc2InstanceList(params)
        echo "InstanceList : ${instanceList}"

        for (instanceID in instanceList) {
            def instanceName = getEc2InstanceName(instanceID)
            def instanceState = getEc2InstanceState(instanceID)

            echo "*** Instance Name of ${instanceID} is ${instanceName} ***"
            echo "*** Instance State of ${instanceName} / ${instanceID} is ${instanceState} ***"

            if (instanceState == "running") {
                try {
                    timeout(time: 1, unit: 'MINUTES') {
                        IS_APPROVED = input(message: "Are you OK to ${params.EC2_ACTION} the instance ${instanceName} / ${instanceID}?",
                                            parameters: [[$class: 'ChoiceParameterDefinition', choices: ['YES', 'NO'].join('\n'), name: 'Select YES / NO']])
                    }
                } catch (exc) {
                    wrap([$class: 'BuildUser']) {
                        error("${BUILD_USER} aborted the approval input to ${params.EC2_ACTION} the instance")
                    }
                }

                if ("${IS_APPROVED}" == "YES") {
                    echo "*** Instance ${instanceName} / ${instanceID} is going to be ${params.EC2_ACTION} ***"
                    sh "aws ec2 ${params.EC2_ACTION}-instances --instance-ids ${instanceID}"
                    def ec2Environment = params.AWS_ENVIRONMENT.toUpperCase()
                    echo "*** EC2 Environment is ${ec2Environment} ***"
                    wrap([$class: 'BuildUser']) {
                        echo "*** Sending Slack Notification ***"
                        slackSend channel: "${SLACK_CHANNEL}", color: (params.EC2_ACTION == 'stop') ? "#FF7F50" : "#9FE2BF",
                                  message: "${BUILD_USER} :: ${params.APP_NAME} :: ${ec2Environment} :: EC2 Instance ${instanceName} / ${instanceID} is ${params.EC2_ACTION}:: ${BUILD_TIMESTAMP}"
                    }
                } else {
                    echo "*** Instance ${instanceName} / ${instanceID} will not be ${params.EC2_ACTION} as per user input ***"
                }
            } else {
                echo "*** Instance ${instanceName} / ${instanceID} is already in ${params.EC2_ACTION} state, so no further action is required ***"
            }
        }
    }
}

def getEc2InstanceList(params) {
    sh "aws ec2 describe-instances --filters Name=tag-value,Values='${params.APP_NAME}' --output text --query 'Reservations[*].Instances[*].InstanceId' > ./instance_id.list"
    readFile(file: './instance_id.list').readLines()
}

def getEc2InstanceName(instanceID) {
    sh "aws ec2 describe-instances --instance-id ${instanceID} --output text --query 'Reservations[*].Instances[*].{Instance_Name:Tags[?Key==`Name`]|[0].Value}'"
            .trim()
}

def getEc2InstanceState(instanceID) {
    sh "aws ec2 describe-instances --instance-id ${instanceID} --output text --query 'Reservations[*].Instances[*].State.Name'"
            .trim()
}
