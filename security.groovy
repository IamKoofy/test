// vars/startStop.groovy

def call(Map params) {
    setBuildName(params)
    performEc2Actions(params)
    displayEc2Instances(params)
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
    def action = params.EC2_ACTION

    withAWS(credentials: "myteam-${params.AWS_ACCOUNT}-${params.AWS_ENVIRONMENT}", region: 'us-east-1') {
        def instanceList = getEc2InstanceList(params)
        echo "InstanceList before ${action}: ${instanceList}"

        for (instanceID in instanceList) {
            def instanceName = getEc2InstanceName(instanceID)
            def instanceState = getEc2InstanceState(instanceID)

            echo "*** Instance Name of ${instanceID} is ${instanceName} ***"
            echo "*** Instance State of ${instanceName} / ${instanceID} is ${instanceState} ***"

            if ((action == 'stop' && instanceState == 'running') || (action == 'start' && instanceState == 'stopped')) {
                try {
                    timeout(time: 1, unit: 'MINUTES') {
                        IS_APPROVED = input(message: "Are you OK to ${action} the instance ${instanceName} / ${instanceID}?",
                                            parameters: [[$class: 'ChoiceParameterDefinition', choices: ['YES', 'NO'].join('\n'), name: 'Select YES / NO']])
                    }
                } catch (exc) {
                    wrap([$class: 'BuildUser']) {
                        error("${BUILD_USER} aborted the approval input to ${action} the instance")
                    }
                }

                if ("${IS_APPROVED}" == "YES") {
                    echo "*** Instance ${instanceName} / ${instanceID} is going to be ${action} ***"
                    sh "aws ec2 ${action}-instances --instance-ids ${instanceID}"
                    def ec2Environment = params.AWS_ENVIRONMENT.toUpperCase()
                    echo "*** EC2 Environment is ${ec2Environment} ***"
                    wrap([$class: 'BuildUser']) {
                        echo "*** Sending Slack Notification ***"
                        slackSend channel: "${SLACK_CHANNEL}", color: (action == 'stop') ? "#FF7F50" : "#9FE2BF",
                                  message: "${BUILD_USER} :: ${params.APP_NAME} :: ${ec2Environment} :: EC2 Instance ${instanceName} / ${instanceID} is ${action}:: ${BUILD_TIMESTAMP}"
                    }
                } else {
                    echo "*** Instance ${instanceName} / ${instanceID} will not be ${action} as per user input ***"
                }
            } else {
                echo "*** Instance ${instanceName} / ${instanceID} is already in ${action} state, so no further action is required ***"
            }
        }

        instanceList = getEc2InstanceList(params)
        echo "InstanceList after ${action}: ${instanceList}"
    }
}

def displayEc2Instances(params) {
    echo "Displaying EC2 instances..."
    withAWS(credentials: "myteam-${params.AWS_ACCOUNT}-${params.AWS_ENVIRONMENT}", region: 'us-east-1') {
        def instanceList = getEc2InstanceList(params)
        echo "InstanceList : ${instanceList}"

        for (instanceID in instanceList) {
            def instanceName = getEc2InstanceName(instanceID)
            def instanceState = getEc2InstanceState(instanceID)

            echo "*** Instance Name of ${instanceID} is ${instanceName} ***"
            echo "*** Instance State of ${instanceName} / ${instanceID} is ${instanceState} ***"
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
