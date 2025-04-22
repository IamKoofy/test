oc get namespaces -l project-owner=gbt -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}'
