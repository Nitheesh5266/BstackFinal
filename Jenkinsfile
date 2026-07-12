pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {

        stage('Build & Test') {
            steps {
                dir('bstackdemo-selenium-cucumber-v3') {
                    bat 'mvn clean test'
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'bstackdemo-selenium-cucumber-v3/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'bstackdemo-selenium-cucumber-v3/target/**/*', fingerprint: true
        }
    }
}
