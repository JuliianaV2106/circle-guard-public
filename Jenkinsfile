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
        stage('Deploy to Kubernetes') {
            when {
                expression { 
                    return env.GIT_BRANCH == 'origin/master' 
                }
            }
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
                        sh "kubectl apply -f k8s/${service}.yaml"
                    }
                    sh "kubectl rollout status deployment/gateway-service -n circleguard --timeout=120s"
                }
            }
        }
    }

   post {
        always {
            echo 'Pipeline finalizado'
            cleanWs()
        }
        success {
            echo 'Pipeline EXITOSO'
            emailext(
                subject: "SUCCESS: Circle Guard Pipeline - Build #${BUILD_NUMBER}",
                body: """
                    <h2>Pipeline Exitoso</h2>
                    <p><b>Job:</b> ${JOB_NAME}</p>
                    <p><b>Build:</b> #${BUILD_NUMBER}</p>
                    <p><b>Branch:</b> ${GIT_BRANCH}</p>
                    <p><b>URL:</b> <a href="${BUILD_URL}">${BUILD_URL}</a></p>
                    <p>Todos los stages completaron exitosamente.</p>
                """,
                mimeType: 'text/html',
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
        failure {
            echo 'Pipeline FALLIDO'
            emailext(
                subject: "FAILED: Circle Guard Pipeline - Build #${BUILD_NUMBER}",
                body: """
                    <h2>Pipeline Fallido</h2>
                    <p><b>Job:</b> ${JOB_NAME}</p>
                    <p><b>Build:</b> #${BUILD_NUMBER}</p>
                    <p><b>Branch:</b> ${GIT_BRANCH}</p>
                    <p><b>URL:</b> <a href="${BUILD_URL}">${BUILD_URL}</a></p>
                    <p>Revisa el console output para ver el error.</p>
                """,
                mimeType: 'text/html',
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
    }
}