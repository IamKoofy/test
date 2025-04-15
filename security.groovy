namespaces_json.stdout | from_json |
        json_query("items[?metadata.labels[\"project-owner\"]=='gbt'].metadata.name")
