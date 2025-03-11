E1OpenShiftUrl: ${{ secrets.ROSA_NONCDE_DEV_URL }}
E1OpenShiftToken: ${{ secrets.DEV_ROSA_TOKEN }}
DeployToCert: true
E2OpenShiftUrl: ${{ secrets.ROSA_NONCDE_CERT_URL }}
E2OpenShiftToken: ${{ secrets.CERT_ROSA_TOKEN }}
OpenShiftProjectName: my-openshift-project
DeploymentConfigName: my-deployment-config
ContainerName: my-container
DockerDevRepo: my-docker-dev-repo
DockerCertRepo: my-docker-cert-repo
CertApprovalEmail: test.Mustafa@myorg.com
