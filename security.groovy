#!/bin/bash

#####################################################################
# Script is used for patching the Knative service with a new image
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
    echo "$(date +"%Y-%m-%d %T") - ${message}" >> "$LOG_FILE"
}

function ERROR {
    message="$*"
    echo "${message}"
    echo "$(date +"%Y-%m-%d %T") - ${message}" >> "$LOG_FILE"
    exit
}

function patch_knative_service {
    TOKEN="$1"
    PROJECT="$2"
    SERVICE_NAME="$3"
    IMAGE="$4"

    # Login to OpenShift cluster
    LOG "${green} Logging in to OpenShift cluster..."
    oc login https://api.dev.ii6q.p1.openshiftapps.com:6443 --token="$T
