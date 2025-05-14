- name: Configure NuGet to use Azure Artifacts feed
        run: |
          dotnet nuget add source ^
            --name "hrgtec" ^
            --username "buildagent" ^
            --password "${{ secrets.AZURE_DEVOPS_PAT }}" ^
            --store-password-in-clear-text ^
            "https://hrgtec.pkgs.visualstudio.com/_packaging/hrgtec/nuget/v3/index.json"
