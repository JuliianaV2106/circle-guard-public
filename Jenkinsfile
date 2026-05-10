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
                sh '''./gradlew cleanTest test --no-daemon \
                    -x :services:circleguard-notification-service:test \
                    -x :services:circleguard-promotion-service:test'''
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    def services = [
                        'auth-service',
                        'gateway-service',
                        'identity-service',
                        'form-service',
                        'notification-service',
                        'dashboard-service'
                    ]
                    for (service in services) {
                        sh "docker build -f Dockerfile.${service} -t circleguard/${service}:latest ."
                    }
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh '''./gradlew sonar --no-daemon \
                        -Dsonar.projectKey=circle-guard \
                        -Dsonar.projectName=circle-guard \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.token=${SONAR_TOKEN}'''
                }
            }
        }
        stage('Trivy Security Scan') {
            steps {
                script {
                    def services = [
                        'auth-service',
                        'gateway-service',
                        'identity-service',
                        'form-service',
                        'notification-service',
                        'dashboard-service'
                    ]
                    for (service in services) {
                        sh """
                            trivy image \
                                --exit-code 0 \
                                --severity HIGH,CRITICAL \
                                --format table \
                                circleguard/${service}:latest
                        """
                    }
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