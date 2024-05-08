#!/bin/bash

#####################################################################
# Script is used for deploying a Knative service in ROSA
#####################################################################

red="[PATCH-ERROR] :"
green="[INFO] :"
yellow="[WARNING] :"

GLOBAL_LOG_DIR="/var/log/Ose_Custom/Log"
LOG_DIR="${GLOBAL_LOG_DIR}/$(basename "$0" | cut -d "." -f1)"
LOG_FILE="${LOG_DIR}/log_file.log"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

function usage1 {
    echo "Usage : $(basename "$0")"
    echo "To run the script, the following fields are mandatory:"
    echo "  -t <token value>"
    echo "  -n <project name>"
    echo "  -s <service name>"
    echo "  -i <image name>"
    exit 100
}

function LOG {
    message="$*"
    echo "${message}"
}

function ERROR {
    message="$*"
    echo "${message}"
    exit
}

function deploy_knative_service {
    TOKEN="$1"
    PROJECT="$2"
    SERVICE_NAME="$3"
    IMAGE="$4"

    # Login to OpenShift cluster
    LOG "${green} Logging in to OpenShift cluster..."
    oc login https://api.dev.ii6q.p1.openshiftapps.com:6443 --token="$TOKEN" --insecure-skip-tls-verify > /dev/null 2>&1

    if [[ $? -ne 0 ]]; then
        LOG "${red} Failed to log in to OpenShift cluster"
        exit 200
    fi

    # Deploy Knative service
    KNATIVE_YAML=$(cat <<EOF
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: $SERVICE_NAME
  namespace: $PROJECT
  labels:
    domain: amex
spec:
  template:
    metadata:
      annotations:
    spec:
      containers:
      - name: $SERVICE_NAME
        image: $IMAGE
        env:
        - name: RESPONSE
          value: "Hello GBTTEAM!"
EOF
)

    LOG "${green} Deploying Knative service..."
    deployment_output=$(echo "$KNATIVE_YAML" | oc apply -f - 2>&1)

    if [[ $? -eq 0 ]]; then
        LOG "${green} Knative service deployed successfully."
    else
        LOG "${red} Deployment of Knative service failed. Error details:"
        LOG "$deployment_output"
        exit 300
    fi
}

while getopts "t:n:s:i:" opt; do
    case $opt in
        t) token="$OPTARG";;
        n) project="$OPTARG";;
        s) service_name="$OPTARG";;
        i) image="$OPTARG";;
        [?] | h | help ) usage1; exit 1;;
    esac
done

if [ -z "$token" ]; then
    LOG "${red} No Token is provided, exiting the script"
    exit 400
elif [ -z "$project" ]; then
    LOG "${red} No Project Name is provided, exiting the script"
    exit 400
elif [ -z "$service_name" ]; then
    LOG "${red} No Service Name is provided, exiting the script"
    exit 400
elif [ -z "$image" ]; then
    LOG "${red} No Image name is provided, exiting the script"
    exit 400
fi

LOG "----------------------------------------------------------- ------------"
LOG "-- Inputs Provided Are --"
LOG "----------------------------------------------------------- ------------"
LOG "${green} Project name is ----> ${project}"
LOG "${green} Service name is ----> ${service_name}"
LOG "${green} Image name is ----> ${image}"

deploy_knative_service "$token" "$project" "$service_name" "$image"
