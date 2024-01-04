pipeline {
    agent any
    parameters {
        choice(name: 'EC2_ACTION', choices: ['start', 'stop'], description: 'Select EC2 Action')
        string(name: 'APP_NAME', defaultValue: '', description: 'Enter Application Name')
        string(name: 'AWS_ENVIRONMENT', defaultValue: '', description: 'Enter AWS Environment')
        booleanParam(name: 'EC2_ALL_INSTANCES', defaultValue: false, description: 'Stop/Start all instances')
    }
    stages {
        stage('Run AWS Actions') {
            steps {
                script {
                    // Call your shared library function and pass params
                    awsActions(
                        EC2_ACTION: params.EC2_ACTION,
                        APP_NAME: params.APP_NAME,
                        AWS_ENVIRONMENT: params.AWS_ENVIRONMENT,
                        EC2_ALL_INSTANCES: params.EC2_ALL_INSTANCES
                    )
                }
            }
        }
    }
}
