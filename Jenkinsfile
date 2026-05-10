pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sonarqube-token')
        REPO_URL    = 'https://github.com/JuliianaV2106/circle-guard-public.git'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'master', url: "${REPO_URL}"
            }
        }
        stage('Build') {
            steps {
                sh './gradlew clean build -x test --no-daemon'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew cleanTest test --no-daemon'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

    }

    post {
        always {
            echo 'Pipeline finalizado'
            cleanWs()
        }
        failure {
            echo 'Pipeline FALLIDO'
        }
        success {
            echo 'Pipeline EXITOSO'
        }
    }
}