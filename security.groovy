 // Return the image tag for later use
        return imageTag
    }
}

def deployToOpenShift(deploymentScript = env.DEPLOYMENT_SCRIPT ?: 'default-deployment-script.sh', imageTag) {
    echo "*** Deploying Application in AWS OpenShift ***"
    echo "${imageTag}"

    // Replace with the actual OpenShift deployment command
    sh "${deploymentScript} -t ${env.AUTH_TOKEN} -n ${env.NAME_SPACE} -d ${DEPLOYMENT_CONFIG} -c ${DEPLOYMENT_CONFIG} -i ${env.NEXUS_REPO}/gtqc/${repository}:${imageTag}"
}
