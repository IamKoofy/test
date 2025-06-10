variables:
  - group: GithubPAT  # Fixed typo from "GihubPAT"
  - name: BuildParameters.ArtifactName
    value: drop
  - name: BuildPlatform
    value: 'Any CPU'
  - name: BuildConfiguration
    value: 'Release'
  - name: Build.BuildId
    value: $(date:yyyyMMdd)$(rev:.r)

jobs:
  - job: Job_1
    displayName: Agent job 1
    pool:
      name: main-build-pool

    steps:
      - task: PowerShell@2
        displayName: 'Clone GitHub Repo'
        inputs:
          targetType: 'inline'
          script: |
            $ErrorActionPreference = 'Stop'

            $token = "$(GithubPAT)"
            if (-not $token) {
              throw "GitHub PAT is missing!"
            }

            $repo = "AEGBT/gbt-apac-PAM-Production"
            $branch = "ABM-Dev"
            $localPath = "gbt-apac-PAM"

            $gitUrl = "https://$token@github.com/$repo.git"

            Write-Host "Cloning from $gitUrl to $localPath..."
            git clone -b $branch $gitUrl $localPath

            Write-Host "Listing cloned files:"
            Get-ChildItem $localPath -Recurse | ForEach-Object {
              Write-Host $_.FullName
            }

      - task: SonarQubePrepare@5
        displayName: Prepare analysis on SonarQube
        enabled: false  # still disabled, but fixed path
        inputs:
          SonarQube: 1ecdfa54-774a-4892-81fd-4d31642bc912
          projectKey: gbt-apac-PAM
          projectName: gbt-apac-PAM
          extraProperties: |
            sonar.projectBaseDir=$(System.DefaultWorkingDirectory)/gbt-apac-PAM

      - task: NuGetCommand@2
        displayName: NuGet restore
        inputs:
          solution: gbt-apac-PAM\PAM.sln
          selectOrConfig: config
          nugetConfigPath: gbt-apac-PAM\nuget.config
          externalEndpoints: 9d6b3432-5c32-4a0e-8620-9a06ff19f0d8

      - task: VSBuild@1
        displayName: Build solution
        inputs:
          solution: gbt-apac-PAM\PAM.sln
          msbuildArgs: >
            /p:DeployOnBuild=true 
            /p:WebPublishMethod=Package 
            /p:PackageAsSingleFile=true 
            /p:SkipInvalidConfigurations=true 
            /p:PackageLocation="$(build.artifactstagingdirectory)\\"
          platform: $(BuildPlatform)
          configuration: $(BuildConfiguration)

      - task: VSTest@2
        displayName: Test Assemblies
        inputs:
          testAssemblyVer2: >
            gbt-apac-PAM\**\bin\$(BuildConfiguration)\**\*test*.dll
            !**\obj\**
          platform: $(BuildPlatform)
          configuration: $(BuildConfiguration)

      - task: SonarQubeAnalyze@5
        displayName: Run Code Analysis
        enabled: false

      - task: SonarQubePublish@5
        displayName: Publish Quality Gate Result
        enabled: false

      - task: PublishSymbols@2
        displayName: Publish symbols path
        continueOnError: true
        inputs:
          SearchPattern: '**\bin\**\*.pdb'
          PublishSymbols: false
          SymbolServerType: TeamServices

      - task: Application security testing@2021
        displayName: Application security testing
        enabled: false
        inputs:
          projectName: ABM-MASTER
          CheckmarxService: 6ac90be7-3609-417a-bd5f-da024882fa3b
          fullTeamName: CxServer\SP\Company\Users\GG-APACAPPTEAMCHECKMARX

      - task: PublishBuildArtifacts@1
        displayName: Publish Artifact
        condition: succeededOrFailed()
        inputs:
          PathtoPublish: $(Build.ArtifactStagingDirectory)
          ArtifactName: $(BuildParameters.ArtifactName)
          TargetPath: '\\my\share\$(Build.DefinitionName)\$(Build.BuildNumber)'
