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