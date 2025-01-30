# User Guide: Pod Restart Request in Freshservice

Overview

This document provides guidance on how to request a pod restart using the Freshservice ticketing system. This automation ensures that the necessary pods are restarted efficiently without manual intervention.

How to Submit a Request

Log in to Freshservice.

Navigate to the Service Catalog.

Select the template ePaaS Pod Start/Stop.

Fill in the following fields with the required details:

Environment: Select the appropriate environment (e.g., NON-CDE, CDE).

Name of the Project: Enter the project name.

Name of the Service: Specify the service name.

Should we restart all pods for the service?: Choose YES or NO.

If NO, provide the pod names: Enter the pod names as a comma-separated list (e.g., pod1-abc123,pod2-def456).

Submit the request.

The automation will handle the pod restart based on the provided details.

# Technical Documentation: Pod Restart Automation

Overview

The pod restart automation retrieves Service Requests (SRs) from Freshservice, extracts necessary details, and triggers an OpenShift pod restart based on the request parameters.

Workflow

Fetch Freshservice SRs: The automation polls Freshservice for SRs that match the relevant criteria.

Extract Request Details: Parses the request fields including environment, project name, service name, restart type, and pod names.

Execute Restart Process:

If "Restart all pods" is YES, it deletes all pods for the specified service.

If "Restart all pods" is NO, it deletes only the specified pods.

Validation: The script waits for the pods to stabilize and verifies the restart was successful.

Inputs from Freshservice

environment: NON-CDE or CDE.

name_of_the_project: The project namespace in OpenShift.

name_of_the_service: The service name associated with the pods.

should_we_restart_all_pods_or_one_pod_at_a_time: YES or NO.

name_of_the_pod: Comma-separated list of pod names (only required if "Restart all pods" is NO).

Key Playbooks

fetch_sr.yml: Retrieves SRs from Freshservice.

process_sr.yml: Extracts relevant fields and initiates the restart process.

restart_pods.yml: Handles the OpenShift login and pod deletion based on request parameters.

Logs & Monitoring

Automation logs are available in AAP for troubleshooting.

Any failures in execution will be captured in the logs and can be reviewed in AWX/AAP.

For any issues, refer to the automation logs or contact the platform team.
