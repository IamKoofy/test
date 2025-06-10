variables:
  - name: BuildParameters.ArtifactName
    value: drop
  - name: BuildPlatform
    value: 'Any CPU'
  - name: BuildConfiguration
    value: 'Release'
  - name: Build.BuildId
    value: $(date:yyyyMMdd)$(rev:.r)
  - group: GithubPAT

jobs:
- job: Job_1
  displayName: Build Job
  pool:
    name: main-build-pool
  variables:
    System.Debug: true
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
        $repo = "AEGBT/gbt-apac-ABM.EmailOffers"
        $branch = "dev"
        $localPath = "APAC-ABM-PROD"
        $gitUrl = "https://$token@github.com/$repo.git"
        Write-Host "Cloning from $gitUrl to $localPath..."
        git clone -b $branch $gitUrl $localPath
        Write-Host "Repo cloned successfully. Listing files:"
        Get-ChildItem -Path $localPath -Recurse | ForEach-Object { Write-Host $_.FullName }

  - task: SonarQubePrepare@5
    displayName: Prepare SonarQube Analysis
    inputs:
      SonarQube: 3c7512f3-eb4b-41d5-918c-a632d362014d
      projectKey: gbtapac-emailoffers-prod
      projectName: gbtapac-emailoffers-prod
      extraProperties: |
        sonar.projectBaseDir=$(System.DefaultWorkingDirectory)/APAC-ABM-PROD
        sonar.branch.name=

  - task: NuGetToolInstaller@1
    inputs:
      versionSpec: '>=6.6.1'

  - task: NuGetCommand@2
    displayName: Restore NuGet packages
    inputs:
      solution: APAC-ABM-PROD\ABM.EmailOffers.sln
      selectOrConfig: config
      nugetConfigPath: APAC-ABM-PROD\nuget.config
      externalEndpoints: 9d6b3432-5c32-4a0e-8620-9a06ff19f0d8

  - task: VSBuild@1
    inputs:
      solution: APAC-ABM-PROD\ABM.EmailOffers.sln
      msbuildArgs: >
        /p:DeployOnBuild=true 
        /p:WebPublishMethod=Package 
        /p:PackageAsSingleFile=true 
        /p:SkipInvalidConfigurations=true 
        /p:PackageLocation="$(build.artifactstagingdirectory)\\"
      platform: $(BuildPlatform)
      configuration: $(BuildConfiguration)

  - task: VisualStudioTestPlatformInstaller@1
    inputs:
      versionSelector: specificVersion
      testPlatformVersion: '>=6.6.1'

  - task: VSTest@2
    displayName: Run Unit Tests
    inputs:
      testAssemblyVer2: >
        APAC-ABM-PROD\**\bin\$(BuildConfiguration)\**\*test*.dll
        !**\obj\**
        !**\ref\**
        !**\xunit*.dll
      vsTestVersion: toolsInstaller
      runSettingsFile: APAC-ABM-PROD\EmailOfferUnittest.runsettings
      codeCoverageEnabled: true

  - task: PublishTestResults@2
    displayName: Publish Test Results
    inputs:
      testRunner: VSTest
      testResultsFiles: '$(System.DefaultWorkingDirectory)\APAC-ABM-PROD\**\TestResults\*.trx'

  - task: SonarQubeAnalyze@5
    displayName: Run SonarQube Code Analysis
    inputs:
      extraProperties: |
        sonar.working.directory=.sonarqube

  - task: SonarQubePublish@5
    displayName: Publish SonarQube Results
    continueOnError: true

  - task: NexusIqPipelineTask@1
    displayName: Nexus IQ Policy Evaluation
    continueOnError: true
    inputs:
      nexusIqService: e80e497e-f9fd-4dd0-bf06-53020dfcb9e3
      organizationId: e2d8254992b344f79a5ad89bce5e7e71
      applicationId: ABM.EMailOffers
      stage: Release
      scanTargets: |
        $(Build.ArtifactStagingDirectory)/**/*.dll
        $(Build.ArtifactStagingDirectory)/**/*.exe

  - task: PublishBuildArtifacts@1
    displayName: Publish Build Artifact
    inputs:
      PathtoPublish: '$(Build.ArtifactStagingDirectory)'
      ArtifactName: '$(BuildParameters.ArtifactName)'
