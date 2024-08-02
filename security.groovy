variables:
  - name: BuildParameters.ArtifactName
    value: drop
  - name: BuildPlatform
    value: 'Any CPU'
  - name: BuildConfiguration
    value: 'Release'
  - name: Build.BuildId
    value: $(date:yyyyMMdd)$(rev:.r)

resources:
  repositories:
    - repository: self
      type: github
      name: CLOUD/your-repo-name
      ref: refs/heads/main # Adjust branch as needed
      endpoint: your-github-service-connection

jobs:
  - job: Job_1
    displayName: Agent job 1
    pool:
      name: main-build-pool

    steps:
      - checkout: self

      - task: SonarQubePrepare@5
        displayName: Prepare analysis on SonarQube
        enabled: False
        inputs:
          SonarQube: 1ecdfa54-774a-4892-81fd-4d31642bc912
          projectKey: gbt-apac-PAM
          projectName: gbt-apac-PAM

      - task: NuGetCommand@2
        displayName: NuGet restore
        inputs:
          solution: PAM.sln
          selectOrConfig: config
          nugetConfigPath: nuget.config
          externalEndpoints: 9d6b3432-5c32-4a0e-8620-9a06ff19f0d8

      - task: VSBuild@1
        displayName: Build solution
        inputs:
          solution: PAM.sln
          msbuildArgs: /p:DeployOnBuild=true /p:WebPublishMethod=Package /p:PackageAsSingleFile=true /p:SkipInvalidConfigurations=true /p:PackageLocation="$(build.artifactstagingdirectory)\\"
          platform: $(BuildPlatform)
          configuration: $(BuildConfiguration)

      - task: VSTest@2
        displayName: Test Assemblies
        inputs:
          testAssemblyVer2: >-
            **\$(BuildConfiguration)\*test*.dll
            !**\obj\**
          platform: $(BuildPlatform)
          configuration: $(BuildConfiguration)

      - task: SonarQubeAnalyze@5
        displayName: Run Code Analysis
        enabled: False

      - task: SonarQubePublish@5
        displayName: Publish Quality Gate Result
        enabled: False

      - task: PublishSymbols@2
        displayName: Publish symbols path
        continueOnError: True
        inputs:
          SearchPattern: '**\bin\**\*.pdb'
          PublishSymbols: false
          SymbolServerType: TeamServices

      - task: Application security testing@2021
        displayName: Application security testing
        enabled: False
        inputs:
          projectName: ABM-MASTER
          CheckmarxService: 6ac90be7-3609-417a-bd5f-da024882fa3b
          fullTeamName: CxServer\SP\Company\Users\GG-APACAPPTEAMCHECKMARX

      - task: PublishBuildArtifacts@1
        displayName: Publish Artifact
        condition: succeededOrFailed()
        inputs:
          PathtoPublish: $(build.artifactstagingdirectory)
          ArtifactName: $(BuildParameters.ArtifactName)
          TargetPath: '\\my\share\$(Build.DefinitionName)\$(Build.BuildNumber)'
