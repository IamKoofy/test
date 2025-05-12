repository_dispatch.

In the application repo, create a small workflow that fires on push or pull_request:

yaml
Copy
Edit
name: Trigger Central Workflow

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

jobs:
  notify-central:
    runs-on: ubuntu-latest
    steps:
      - name: Trigger central workflow
        run: |
          curl -X POST \
            -H "Accept: application/vnd.github+json" \
            -H "Authorization: Bearer ${{ secrets.CENTRAL_PAT }}" \
            https://api.github.com/repos/AEGBT/hrg-devops-pipelines/dispatches \
            -d '{"event_type":"adservice-pipeline","client_payload":{"ref":"refs/heads/main","repository":"AEGBT/MilkyWay-TravelConfirmation"}}'
In the central pipeline repo, define a workflow that listens to this custom event:

yaml
Copy
Edit
name: AdService Pipeline

on:
  repository_dispatch:
    types: [adservice-pipeline]

jobs:
  run-pipeline:
    runs-on: windows-latest
    steps:
      - name: Checkout source code
        uses: actions/checkout@v4
        with:
          repository: AEGBT/MilkyWay-TravelConfirmation
          ref: ${{ github.event.client_payload.ref }}
          token: ${{ secrets.CENTRAL_PAT }}
      - name: Call composite action
        uses: ./.github/actions/adservice-build
Note: You'll need a Personal Access Token (PAT) in both repos stored as CENTRAL_PAT that has workflow and repo scopes.

