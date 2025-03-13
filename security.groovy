# Build Variables File

GithubRepo: ""  # GitHub repository name
Branch: ""  # Branch to build from
SonarprojectBaseDir: ""  # Base directory for SonarQube analysis
MajorVersion: ""  # Major version number
MinorVersion: ""  # Minor version number
BuildNumber: ""  # Build number
Rev: ""  # Revision number
SolutionFile: ""  # Path to the solution (.sln) file
PathToMainProject: ""  # Path to the main project directory
PathToTestProject: ""  # Path to the test project directory
ProjectPath: ""  # Path to the project being built
DockerFile: ""  # Path to the Dockerfile
DockerRepo: ""  # Docker repository URL
DockerImageName: ""  # Name of the Docker image
SonarQubeProjectName: ""  # SonarQube project name
SonarQubeExclusions: ""  # File patterns to exclude from SonarQube analysis
SonarQubeCoverletReportPaths: ""  # Paths to coverage report for SonarQube
DotnetBuildOutputDockerSrc: ""  # Build output path for Docker
SnykAuthToken: ""  # Snyk authentication token
SnykOrg: ""  # Snyk organization name
SnykProjectName: ""  # Snyk project name
SnykScanEnabled: "false"  # Enable/disable Snyk security scan

# Additional variables based on GitHub Actions workflow
DockerDevRepo: ""  # Development Docker repository
DockerFolder: ""  # Folder for Docker build context
SonarQubeCoverageReportPaths: ""  # Coverage report paths for SonarQube
SonarQubeTextExecutionReportPaths: ""  # Text execution report paths for SonarQube
SonarQubeSources: ""  # Source files for SonarQube analysis
SonarQubeTestSources: ""  # Test source files for SonarQube analysis
EnvFilePath: ""  # Path to environment file for build
EnvBuildNumberUpdateEnabled: "true"  # Whether to update build number in env file
EnvBuildNumberFieldName: "REACT_APP_ELT_VERSION"  # Field name for build version update
LintingEnabled: "false"  # Enable/disable linting
TypescriptCheckingEnabled: "false"  # Enable/disable TypeScript checking
YarnBuildArgs: "build"  # Build arguments for Yarn

# Docker login credentials (to be set via GitHub Secrets)
docker_username: ${{ secrets.DOCKER_USERNAME }}
docker_password: ${{ secrets.DOCKER_PASSWORD }}

# GitHub authentication tokens (to be set via GitHub Secrets)
git_access_token: ${{ secrets.GIT_ACCESS_TOKEN }}
GIT_PAT: ${{ secrets.GIT_PAT }}

# SonarQube authentication token (to be set via GitHub Secrets)
SonarQubeToken: ${{ secrets.SONARQUBE_TOKEN }}
