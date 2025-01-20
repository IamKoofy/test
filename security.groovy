    oc get pods -n {{ sr_project }} -l app={{ sr_service }} -o jsonpath="{.items[?(@.status.phase=='Running')].metadata.name}" | wc -w
