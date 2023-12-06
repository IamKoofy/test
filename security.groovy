def sonarScanMaven() {
    // SonarQube analysis for Maven projects
    withSonarQubeEnv('SonarQube-Container') {
        echo "*** Analysing Code using SonarQube ***"
        sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar -Dsonar.projectKey=' + env.SONAR_PROJECT_KEY + ' -Dsonar.projectName=' + env.SONAR_PROJECT_NAME
    }
}
