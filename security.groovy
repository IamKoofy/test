#!/bin/bash

#####################################################################
# Script is used for patching the Knative service with a new image
#####################################################################

red="[PATCH-ERROR] :"
green="[INFO] :"
yellow="[WARNING] :"

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
    echo "${red}${message}" >&2
    exit
}

function patch_knative_service {
    TOKEN="$1"
    PROJECT="$2"
    SERVICE_NAME="$3"
    IMAGE="$4"

    # Login to OpenShift cluster
    LOG "${green} Logging in to OpenShift cluster..."
    oc login https://api.dev.ii6q.p1.openshiftapps.com:6443 --token="$TOKEN" --insecure-skip-tls-verify > /dev/null 2>&1 || {
        ERROR "Failed to log in to OpenShift cluster"
    }

    # Get the current service revision
    LOG "${green} Retrieving current service revision..."
    CURRENT_REVISION=$(kubectl get revision -l serving.knative.dev/service="$SERVICE_NAME" -n "$PROJECT" -o jsonpath='{.items[0].metadata.name}')
    
    if [ -z "$CURRENT_REVISION" ]; then
        ERROR "Failed to retrieve current service revision"
   
