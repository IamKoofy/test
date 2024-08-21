{{
        ticket_data.results[0].json.requested_items |
        map(attribute='custom_fields') |
        map(lambda cf: {
          'environment': cf.environment,
          'platform': cf.platform,
          'account_name_to_be_unlocked': cf.account_name_to_be_unlocked
        }) |
        list
      }}
