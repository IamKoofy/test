def call() {
    // Define the order of steps
    mavenBuild()
    snykscan()
    nexusscan()
    sonarScanMaven()
    def imageTag = dockerBuildAndPush()
    deployToOpenShift(imageTag: imageTag)
    echo 'All stages completed.'
}
