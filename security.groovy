stages {
    stage('GitCheckOut') {
        steps {
            echo "*** Begin Checking out Code from GitHub ***"
            git branch: params.GIT_BRANCH, credentialsId: params.GIT_USER, url: params.GIT_URL
            echo "*** End Checking out Code from GitHub ***"
        }
    }
}
