def call(Map params) {
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
    def ec2Action = params.EC2_ACTION.toLowerCase()
    def ec2Instances = getEc2Instances(params)

    for (ec2Instance in ec2Instances) {
        handleEc2Instance(ec2Instance, ec2Action, params)
    }
}

def performEc2ActionsSelectedInstances(params) {
    echo "Performing EC2 actions on selected instances..."
    def ec2Action = params.EC2_ACTION.toLowerCase()
    def ec2Instances = getEc2Instances(params)

    for (ec2Instance in ec2Instances) {
        handleEc2Instance(ec2Instance, ec2Action, params)
    }
}

def handleEc2Instance(ec2Instance, ec2Action, params) {
    def instanceID = ec2Instance['instanceId']
    def instanceName = ec2Instance['instanceName']
    def instanceState = ec2Instance['instanceState']

    echo "*** Instance Name of ${instanceID} is ${instanceName} ***"
    echo "*** Instance State of ${instanceName} / ${instanceID} is ${instanceState} ***"

    if ((instanceState == "running" && ec2Action == "stop") || (instanceState == "stopped" && ec2Action == "start")) {
        try {
            timeout(time: 1, unit: 'MINUTES') {
                IS_APPROVED = input(message: "Are you OK to ${ec2Action} the instance ${instanceName} / ${instanceID}?",
                                    parameters: [[$class: 'ChoiceParameterDefinition', choices: ['YES', 'NO'].join('\n'), name: 'Select YES / NO']])
            }
        } catch (exc) {
            wrap([$class: 'BuildUser']) {
                error("${BUILD_USER} aborted the approval input to ${ec2Action} the instance")
            }
        }

        if ("${IS_APPROVED}" == "YES") {
            echo "*** Instance ${instanceName} / ${instanceID} is going to be ${ec2Action} ***"
            sh "aws ec2 ${ec2Action}-instances --instance-ids ${instanceID}"
            def ec2Environment = params.AWS_ENVIRONMENT.toUpperCase()
            echo "*** EC2 Environment is ${ec2Environment} ***"
            wrap([$class: 'BuildUser']) {
                echo "*** Sending Slack Notification ***"
                slackSend channel: "${SLACK_CHANNEL}", color: (ec2Action == 'stop') ? "#FF7F50" : "#9FE2BF",
                          message: "${BUILD_USER} :: ${params.APP_NAME} :: ${ec2Environment} :: EC2 Instance ${instanceName} / ${instanceID} is ${ec2Action}:: ${BUILD_TIMESTAMP}"
            }
        } else {
            echo "*** Instance ${instanceName} / ${instanceID} will not be ${ec2Action} as per user input ***"
        }
    } else {
        echo "*** Instance ${instanceName} / ${instanceID} is already in ${instanceState} state, so no further action is required ***"
    }
}

def getEc2Instances(params) {
    def ec2Instances = []
    withAWS(credentials: "myteam-${params.AWS_ACCOUNT}-${params.AWS_ENVIRONMENT}", region: 'us-east-1') {
        sh "aws ec2 describe-instances --filters Name=tag-value,Values='${params.APP_NAME}' --output json > ./instance_list.json"
        def instanceList = readJSON file: './instance_list.json'

        instanceList.Reservations.each { reservation ->
            reservation.Instances.each { instance ->
                def ec2Instance = [:]
                ec2Instance['instanceId'] = instance.InstanceId
                ec2Instance['instanceName'] = instance.Tags.find { it.Key == 'Name' }?.Value ?: instance.InstanceId
                ec2Instance['instanceState'] = instance.State.Name
                ec2Instances.add(ec2Instance)
            }
        }
    }
    return ec2Instances
}
