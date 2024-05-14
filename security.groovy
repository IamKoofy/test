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

    # Patch Knative service with the new image
    LOG "${green} Patching Knative service with the new image..."
    kn service update "${SERVICE_NAME}" --image="${IMAGE}" -n "${PROJECT}" > /dev/null 2>&1 || {
        ERROR "Patching Knative service failed."
    }

    LOG "${green} Knative service patched successfully with the new image."
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
    ERROR "No Token is provided, exiting the script"
elif [ -z "$project" ]; then
    ERROR "No Project Name is provided, exiting the script"
elif [ -z "$service_name" ]; then
    ERROR "No Service Name is provided, exiting the script"
elif [ -z "$image" ]; then
    ERROR "No Image name is provided, exiting the script"
fi

LOG "----------------------------------------------------------- ------------"
LOG "-- Inputs Provided Are --"
LOG "----------------------------------------------------------- ------------"
LOG "${green} Project name is ----> ${project}"
LOG "${green} Service name is ----> ${service_name}"
LOG "${green} Image name is ----> ${image}"

patch_knative_service "$token" "$project" "$service_name" "$image"
