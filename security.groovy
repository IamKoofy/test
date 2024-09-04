{% if user_check_response.status == 401 %}
          Account {{ username }} is not found/incorrect please verify or check with MFT team
          {% elif result.status != 200 %}
          Account {{ username }} unlock could not be completed, MFT team in CC will investigate further and reach out to you
          {% endif %}
