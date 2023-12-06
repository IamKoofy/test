def dockerBuildAndPush() {
    // Replace 'your-image-name' with the actual repository name
    def repository = env.REPOSITORY_NAME ?: 'your-image-name'
    
    // Docker build and push
    script {
        echo 'Building Docker Image...'
        def buildNumber = currentBuild.number
        def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        def imageTag = "${repository}:${commitId}-${buildNumber}"

        def app = docker.build(imageTag, "-f Dockerfile .")
        
        echo 'Image Build Completed'
        
        // Push the Docker image to Nexus or your Docker registry
        docker.withRegistry(env.NexusDockerRepo, 'NEXUS-REPO-CREDENTIALS') {
            app.push()
        }
    }
}
