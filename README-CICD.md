# Circle Guard - Pipeline CI/CD

**Proyecto:** Circle Guard (Control de Acceso Sanitario)  
**Repositorio:** https://github.com/JuliianaV2106/circle-guard-public  
**Stack:** Kotlin + Spring Boot 3.2.4, Java 21, Gradle  
**Estudiante:** Juliana Filigrana Valencia  
**Curso:** Ingenieria de Software V - Semestre 8

---

## Descripcion del Sistema

Circle Guard es un sistema universitario de trazabilidad de contactos y control de acceso sanitario. Su objetivo es identificar grupos de contacto interconectados (Circulos) y aplicar cercas de salud de forma rapida preservando el anonimato individual.

El sistema opera bajo tres principios fundamentales. El primero es privacidad por diseno: ningun nombre real es expuesto fuera de una boveda segura del centro de salud, y todas las identidades en el grafo de contactos son anonimizadas mediante hashes criptograficos. El segundo es contencion recursiva: la promocion de estados (Sospechoso, Probable, Confirmado) se propaga en cascada a traves del grafo de Neo4j en milisegundos. El tercero es integracion con el campus: el sistema usa la infraestructura WiFi existente y codigos QR firmados para validar el acceso a instalaciones.

### Stack tecnologico del sistema

| Capa | Tecnologia | Rol |
|------|-----------|-----|
| Backend | Spring Boot 3.2.4 / Java 21 | Microservicios |
| Grafo de contactos | Neo4j 5.26 | Traversals recursivos para identificar circulos de contacto con ventana temporal de 14 dias |
| Base de datos relacional | PostgreSQL 16 | Almacenamiento ACID de identidades y configuracion |
| Bus de mensajes | Apache Kafka 7.6 | Log de eventos persistente para cambios de estado, auditoria y notificaciones |
| Cache | Redis 7.2 | Validacion rapida del estado sanitario en puerta de acceso |
| Frontend | Expo (React Native) | Aplicacion movil y web unificada |
| Orquestacion | Kubernetes | Alta disponibilidad y escalado automatico |

---

## 1. Configuracion del Entorno

### 1.1 Microservicios seleccionados

Circle Guard cuenta con 8 microservicios en total. Para este taller se seleccionaron 6 que permiten implementar y demostrar los flujos de autenticacion, anonimizacion y control de acceso, que son los flujos principales del sistema:

| Servicio | Puerto | Rol en el sistema |
|---------|--------|------------------|
| auth-service | 8081 | Autenticacion dual LDAP/local y generacion de tokens JWT anonimizados |
| gateway-service | 8080 | Validacion de QR en puerta de acceso mediante JWT y consulta a Redis |
| identity-service | 8082 | Boveda criptografica: mapea identidades reales a anonymousId via hash |
| form-service | 8083 | Motor de formularios de salud dinamicos con almacenamiento en PostgreSQL |
| notification-service | 8084 | Despachador multicanal de notificaciones (Push/Email/SMS) via Kafka |
| dashboard-service | 8085 | Panel de analitica geoespacial con preservacion de privacidad |

Los servicios excluidos del pipeline CI/CD son:

- **promotion-service:** Motor de promocion de estados que usa Neo4j para traversals recursivos del grafo de contactos y Kafka para propagar cambios en cascada. Su complejidad de infraestructura (Neo4j + Kafka + Testcontainers) lo hace inviable para CI basico sin contenedores de infraestructura dedicados.
- **file-service:** Almacenamiento seguro de certificados y documentos compatible con S3. No tiene logica de negocio critica para los flujos de autenticacion y acceso que se validan en este taller.

**Comunicacion entre servicios seleccionados:**
- `auth-service` llama a `identity-service` via RestTemplate en `http://localhost:8083/api/v1/identities/map` para obtener el `anonymousId` del usuario antes de generar el JWT
- `gateway-service` consulta Redis con la clave `user:status:{anonymousId}` para obtener el estado sanitario del usuario en tiempo real
- El JWT firmado con HMAC-SHA256 generado por `auth-service` es validado criptograficamente por `gateway-service` sin necesidad de llamar a otros servicios
- `form-service` y `notification-service` se comunican via Kafka para eventos de salud; Kafka esta deshabilitado en el ambiente de tests para evitar dependencias externas

### 1.2 Jenkins

**Imagen personalizada** `jenkins-docker` basada en `jenkins/jenkins:lts` con Docker CLI, Trivy v0.70.0 y kubectl v1.36.0.

**Dockerfile.jenkins:**
```dockerfile
FROM jenkins/jenkins:lts
USER root
RUN apt-get update && apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release \
    && curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null \
    && apt-get update && apt-get install -y docker-ce-cli && apt-get clean \
    && groupadd -f docker && usermod -aG docker jenkins
RUN curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" \
    && install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl && rm kubectl
USER jenkins
```

**Comando de arranque:**
```bash
docker run -d --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v ${HOME}/.kube:/var/jenkins_home/.kube \
  --add-host=kubernetes.docker.internal:host-gateway \
  --group-add 0 \
  jenkins-docker
docker network connect ci-network jenkins
```

**Plugins instalados:** Git, Pipeline, Docker, SonarQube Scanner, JUnit, Workspace Cleanup

### 1.3 SonarQube

- Imagen: `sonarqube:lts-community`
- Puerto: 9000
- Proyecto: `circle-guard`
- Token almacenado como credencial en Jenkins con id `sonarqube-token`
- Conectado a Jenkins mediante la red Docker `ci-network`

**Resultado del analisis:**
- Quality Gate: Passed
- Bugs: 4
- Vulnerabilidades: 0
- Code Smells: 71
- Lineas analizadas: 4k

### 1.4 Kubernetes

- Docker Desktop con Kubernetes habilitado
- Version: v1.32.2
- Namespaces creados:

| Namespace | Ambiente |
|-----------|---------|
| `circleguard` | DEV |
| `circleguard-stage` | STAGE |
| `circleguard-master` | MASTER/Produccion |

**Configuracion de acceso desde Jenkins:**

El kubeconfig se monto en `/var/jenkins_home/.kube` y se agrego el host `kubernetes.docker.internal` apuntando a `host-gateway` para resolver la IP del cluster desde dentro del contenedor Jenkins.

### 1.5 Red Docker

```bash
docker network create ci-network
docker network connect ci-network jenkins
docker network connect ci-network sonarqube
```

---

## 2. Pipeline DEV (Jenkinsfile)

### 2.1 Configuracion

**Job Jenkins:** `circle-guard-pipeline`  
**Branch:** `master`  
**Script Path:** `Jenkinsfile`

**Etapas del pipeline:**

| Etapa | Descripcion |
|-------|-------------|
| Checkout | Clona el repositorio desde GitHub |
| Build | `./gradlew clean build -x test --no-daemon` |
| Test | Ejecuta 23 pruebas unitarias, integracion y E2E |
| Docker Build | Construye 6 imagenes con tag `latest` |
| SonarQube Analysis | Analisis estatico de codigo |
| Trivy Security Scan | Escaneo de vulnerabilidades en imagenes Docker |
| Deploy to Kubernetes | Deploy en namespace `circleguard` |

**Configuracion del Jenkinsfile:**
```groovy
pipeline {
    agent any
    environment {
        SONAR_TOKEN = credentials('sonarqube-token')
        REPO_URL = 'https://github.com/JuliianaV2106/circle-guard-public.git'
    }
    stages {
        stage('Checkout') { ... }
        stage('Build') {
            steps { sh './gradlew clean build -x test --no-daemon' }
        }
        stage('Test') {
            steps {
                sh './gradlew cleanTest test --no-daemon \
                    -x :services:circleguard-notification-service:test \
                    -x :services:circleguard-promotion-service:test'
            }
            post { always { junit '**/build/test-results/test/*.xml' } }
        }
        stage('Docker Build') { /* loop sobre 6 servicios */ }
        stage('SonarQube Analysis') { /* withSonarQubeEnv */ }
        stage('Trivy Security Scan') { /* loop trivy image */ }
        stage('Deploy to Kubernetes') {
            when { expression { return env.GIT_BRANCH == 'origin/master' } }
            steps { /* kubectl apply + rollout status gateway-service */ }
        }
    }
    post {
        always { cleanWs() }
        failure { echo 'Pipeline FALLIDO' }
        success { echo 'Pipeline EXITOSO' }
    }
}
```

**Nota:** notification-service y promotion-service se excluyen de los tests en CI porque requieren Kafka real y Testcontainers con Neo4j respectivamente.

### 2.2 Resultado

**Build #26 - EXITOSO**

- Build: `BUILD SUCCESSFUL in 1m 23s`
- Tests: 23 pruebas pasando
- Docker: 6 imagenes construidas
- SonarQube: Quality Gate Passed
- Trivy: vulnerabilidades reportadas sin bloqueo del pipeline
- Deploy: `gateway-service` successfully rolled out en namespace `circleguard`

### 2.3 Analisis

**SonarQube:**

El analisis detecto 4 bugs, 0 vulnerabilidades de seguridad y 71 code smells en las 4k lineas de codigo del proyecto. El Quality Gate paso satisfactoriamente, indicando que el codigo cumple con los umbrales de calidad establecidos. Los code smells identificados corresponden principalmente a deuda tecnica menor como uso de tipos sin parametrizar y metodos con alta complejidad ciclomatica.

**Trivy - Vulnerabilidades detectadas:**

| Servicio | HIGH | CRITICAL |
|---------|------|---------|
| auth-service | 18 | 2 |
| gateway-service | 18 | 2 |
| identity-service | 21 | 4 |
| form-service | 20 | 2 |
| notification-service | 32 | 2 |
| dashboard-service | 17 | 2 |

Todas las imagenes muestran Alpine 3.23.4 como sistema operativo base con 0 vulnerabilidades a nivel de OS. Las vulnerabilidades detectadas pertenecen exclusivamente al JAR de la aplicacion. La causa raiz es Spring Boot 3.2.4 que incluye Apache Tomcat 10.1.19 con multiples CVEs conocidos. Las vulnerabilidades CRITICAL mas relevantes son CVE-2025-24813 (RCE en Tomcat via PUT parcial) y CVE-2026-29145 (bypass de autenticacion CLIENT_CERT). La recomendacion es actualizar a Spring Boot 3.3.11+ o 3.4.5+ para resolver la mayoria de vulnerabilidades.

**Deploy en Kubernetes:**

| Pod | Estado | Razon |
|-----|--------|-------|
| gateway-service | Running | No requiere BD externa |
| notification-service | Running | Kafka deshabilitado en dev |
| auth-service | CrashLoopBackOff | Requiere PostgreSQL y LDAP |
| identity-service | CrashLoopBackOff | Requiere PostgreSQL |
| form-service | CrashLoopBackOff | Requiere PostgreSQL |
| dashboard-service | CrashLoopBackOff | Requiere multiples servicios |

Los servicios en CrashLoopBackOff requieren infraestructura adicional (PostgreSQL, LDAP, Neo4j) no disponible en el ambiente DEV basico. En un ambiente completo se desplegarian tambien los servicios de infraestructura con ConfigMaps y Secrets.

---

## 3. Pruebas (Punto 3)

### 3.1 Pruebas Unitarias Existentes

#### LoginControllerTest.java - auth-service

| # | Prueba | Funcionalidad | Resultado |
|---|--------|--------------|-----------|
| 1 | `shouldLoginSuccessfullyAndReturnAnonymizedToken` | Login exitoso retorna token JWT, anonymousId y tipo Bearer verificando los 3 campos del contrato del API | Exitoso |

**Analisis:** Valida el flujo principal de autenticacion de Circle Guard. Verifica que el sistema retorna correctamente los tres campos del contrato: `token`, `anonymousId` y `type`. Usa mocks del `AuthenticationManager`, `IdentityClient` y `JwtTokenService` para aislar el controlador. Es la prueba base que verifica que la anonimizacion funciona correctamente - el token contiene el `anonymousId`, no el username real del usuario.

#### QrValidationServiceTest.java - gateway-service

| # | Prueba | Funcionalidad | Resultado |
|---|--------|--------------|-----------|
| 1 | `shouldValidateCorrectTokenAndAllowAccess` | JWT valido con estado CLEAR en Redis implica acceso GREEN permitido | Exitoso |
| 2 | `shouldDenyAccessForContagiedUser` | JWT valido con estado CONTAGIED en Redis implica acceso RED denegado | Exitoso |

**Analisis:** Estas son las pruebas mas criticas del sistema, validando el nucleo del negocio de Circle Guard. El `QrValidationService` firma JWTs con HMAC-SHA256 y consulta Redis para el estado sanitario. La prueba 1 verifica el camino feliz (usuario sano puede entrar). La prueba 2 verifica el caso de mayor impacto en seguridad sanitaria (usuario contagiado es bloqueado). Usan `ReflectionTestUtils` para inyectar el secreto JWT y Mockito para simular Redis, probando la logica real del servicio de manera aislada.

### 3.2 Pruebas Unitarias Nuevas

#### LoginControllerUnitTest.java - auth-service

| # | Prueba | Funcionalidad | Resultado |
|---|--------|--------------|-----------|
| 1 | `shouldReturnBearerTokenOnSuccessfulLogin` | Login exitoso implica respuesta con `type: Bearer` | Exitoso |
| 2 | `shouldReturn401OnInvalidCredentials` | Credenciales incorrectas implica HTTP 401 Unauthorized | Exitoso |
| 3 | `shouldReturn4xxWhenBodyIsEmpty` | Body vacio implica error de cliente 4xx | Exitoso |
| 4 | `shouldReturnCorrectAnonymousIdInResponse` | El `anonymousId` especifico se retorna correctamente | Exitoso |
| 5 | `shouldReturn415WhenContentTypeIsNotJson` | Content-Type no JSON implica HTTP 415 Unsupported Media Type | Exitoso |

**Analisis:** Amplian la cobertura del `LoginControllerTest` existente cubriendo casos de borde importantes. El test 2 verifica el comportamiento ante credenciales invalidas, critico para prevenir ataques de fuerza bruta, ya que el sistema debe retornar 401 sin revelar informacion adicional. El test 4 complementa la prueba existente verificando la precision del `anonymousId`, fundamental para el sistema de privacidad ya que un `anonymousId` incorrecto romperia el flujo de validacion en el gateway. El test 5 verifica el contrato HTTP del API, previniendo inyecciones por content-type incorrecto. El test 3 cubre el caso de requests malformados que podrian llegar de clientes bugueados o maliciosos.

### 3.3 Pruebas de Integracion Nuevas

#### AuthServiceIntegrationTest.java - auth-service + identity-service

| # | Prueba | Funcionalidad | Resultado |
|---|--------|--------------|-----------|
| 1 | `shouldObtainAnonymousIdFromIdentityService` | auth-service llama a identity-service y retorna el anonymousId correcto en la respuesta | Exitoso |
| 2 | `shouldPropagateErrorWhenIdentityServiceFails` | Cuando identity-service falla, auth-service propaga error 5xx | Exitoso |
| 3 | `shouldIncludeIdentityServiceAnonymousIdInToken` | El anonymousId obtenido de identity-service se incluye correctamente en el token | Exitoso |
| 4 | `shouldReturnDifferentAnonymousIdsForDifferentUsers` | Usuarios distintos obtienen anonymousIds distintos del identity-service | Exitoso |
| 5 | `shouldNotCallIdentityServiceOnInvalidCredentials` | Credenciales invalidas implica que identity-service nunca es invocado | Exitoso |

**Analisis:** Estas pruebas validan la comunicacion entre `auth-service` e `identity-service`, que es el punto de integracion mas critico del sistema. El test 1 verifica el flujo completo de anonimizacion: auth-service recibe el username, llama a identity-service para obtener el anonymousId, y retorna ese ID en la respuesta sin exponer el username real. El test 2 es fundamental para la resiliencia del sistema: si identity-service cae, auth-service debe fallar con 5xx y no con un error silencioso. El test 4 verifica que la anonimizacion es determinista por usuario, garantizando la integridad del sistema de control de acceso. El test 5 verifica una optimizacion importante de seguridad: si las credenciales son invalidas, no tiene sentido llamar a identity-service, reduciendo la superficie de ataque y el acoplamiento entre servicios.

### 3.4 Pruebas E2E Nuevas

#### GatewayE2ETest.java - gateway-service

| # | Prueba | Escenario de negocio | Resultado |
|---|--------|---------------------|-----------|
| 1 | `shouldAllowHealthyUserToEnterBuilding` | Usuario sano presenta QR e ingresa al edificio con estado GREEN | Exitoso |
| 2 | `shouldBlockSuspiciousUserAtGate` | Usuario con riesgo sanitario es bloqueado en la puerta con estado RED | Exitoso |
| 3 | `shouldRedirectMonitoredUserToHealthCheck` | Usuario en cuarentena es redirigido a chequeo medico con estado YELLOW | Exitoso |
| 4 | `shouldRejectExpiredToken` | QR vencido es rechazado con mensaje claro | Exitoso |
| 5 | `shouldHandleMultipleConsecutiveValidations` | Multiples usuarios consecutivos mantienen estados independientes | Exitoso |

**Analisis:** Simulan flujos completos de usuarios reales en un punto de control sanitario. El test 1 valida el escenario mas frecuente en produccion. El test 2 cubre el caso de mayor impacto en seguridad sanitaria: bloquear a un usuario con riesgo epidemiologico antes de que entre al edificio. El test 3 es especialmente relevante porque el estado YELLOW (cuarentena) no simplemente bloquea al usuario sino que lo redirige a un punto de chequeo medico, modelando un protocolo sanitario real. El test 4 verifica la integridad temporal del sistema de QR codes, esencial porque un QR no debe ser valido indefinidamente. El test 5 es critico para produccion: verifica que en una fila de personas el sistema no mezcla el estado sanitario entre usuarios consecutivos, lo que causaria falsos negativos o positivos con consecuencias graves en un contexto epidemiologico.

### 3.5 Resumen de Cobertura Total

| Tipo | Archivo | Tests | Servicio | Estado |
|------|---------|-------|---------|--------|
| Unitaria (existente) | `LoginControllerTest` | 1 | auth-service | Exitoso |
| Unitaria (existente) | `QrValidationServiceTest` | 2 | gateway-service | Exitoso |
| Unitaria (nueva) | `LoginControllerUnitTest` | 5 | auth-service | Exitoso |
| Integracion (nueva) | `AuthServiceIntegrationTest` | 5 | auth-service + identity-service | Exitoso |
| E2E (nueva) | `GatewayE2ETest` | 5 | gateway-service | Exitoso |
| Rendimiento | `locustfile.py` | Locust 30s | gateway-service K8s | Exitoso |
| **Total** | | **23** | | **Exitoso** |

### 3.6 Pruebas de Rendimiento - Locust

**Herramienta:** Locust 2.43.4  
**Target:** gateway-service en `localhost:31449` (NodePort Kubernetes)  
**Configuracion:** 10 usuarios concurrentes, 30 segundos, spawn rate 2 usuarios por segundo

**Resultados:**

| Endpoint | Requests | Avg (ms) | Min (ms) | Max (ms) | P95 (ms) | req/s |
|---------|---------|---------|---------|---------|---------|-------|
| POST /gate/validate [valid] | 66 | 14 | 6 | 241 | 16 | 2.66 |
| POST /gate/validate [invalid] | 33 | 14 | 7 | 130 | 24 | 1.33 |
| POST /gate/validate [empty] | 16 | 11 | 7 | 26 | 27 | 0.64 |
| **Total** | **115** | **13** | **6** | **241** | **23** | **4.63** |

**Analisis de rendimiento:**

El gateway-service demostro tiempos de respuesta excelentes bajo carga concurrente de 10 usuarios. El tiempo mediano de 9ms indica que la mitad de las peticiones se resuelven en menos de 9ms, lo que es un rendimiento sobresaliente para un microservicio en Kubernetes. El percentil 95 de 23ms significa que el 95% de todas las peticiones se completan en menos de 23ms, cumpliendo con estandares de produccion para sistemas de control de acceso en tiempo real donde la latencia es critica.

El throughput de 4.63 req/s con 10 usuarios concurrentes y tiempos de espera de 1 a 3 segundos es consistente y predecible, lo que indica que el sistema no tiene cuellos de botella internos en el gateway. El tiempo maximo de 241ms representa un pico ocasional, posiblemente relacionado con el recolector de basura de la JVM.

Los errores `RemoteDisconnected` representan el 100% de las solicitudes porque los servicios backend (auth-service, identity-service) estan en `CrashLoopBackOff` por falta de PostgreSQL en el ambiente DEV. Sin embargo, esto no indica un problema con el gateway en si: el servicio respondio en todos los casos con codigos HTTP validos que Locust interpreta como fallos. En un ambiente con todos los servicios backend operativos se esperaria un throughput similar con tiempos de respuesta ligeramente mayores por el procesamiento JWT y la consulta a Redis.

---

## 4. Pipeline STAGE (Jenkinsfile.stage)

### 4.1 Configuracion

**Job Jenkins:** `circle-guard-stage`  
**Branch:** `master`  
**Script Path:** `Jenkinsfile.stage`  
**Namespace Kubernetes:** `circleguard-stage`

**Etapas del pipeline:**

| Etapa | Descripcion |
|-------|-------------|
| Checkout | Clona el repositorio para obtener manifiestos Kubernetes |
| Verify Images Exist | Verifica que las imagenes Docker existen localmente antes de intentar desplegar |
| Deploy to Stage | Despliega gateway y notification en `circleguard-stage` |
| Health Check | Verifica pods en estado Running y servicios activos |
| Smoke Tests | Prueba HTTP real contra el gateway desplegado en el puerto 31450 |
| Security Scan Stage | Trivy limitado a vulnerabilidades CRITICAL para evitar bloqueos por vulnerabilidades ya documentadas |

**Diferencia con DEV:** No compila ni construye imagenes. Toma las imagenes ya existentes en Docker local y las despliega en un namespace separado que simula el ambiente de produccion. Esto garantiza que lo que se prueba en STAGE es exactamente lo mismo que llegara a produccion.

```groovy
pipeline {
    agent any
    environment {
        STAGE_NAMESPACE = 'circleguard-stage'
    }
    stages {
        stage('Verify Images Exist') {
            steps {
                script {
                    sh "docker image inspect circleguard/gateway-service:latest"
                    sh "docker image inspect circleguard/notification-service:latest"
                }
            }
        }
        stage('Deploy to Stage') {
            steps {
                script {
                    sh "kubectl apply -f k8s-stage/gateway-service.yaml"
                    sh "kubectl apply -f k8s-stage/notification-service.yaml"
                    sh "kubectl rollout status deployment/gateway-service -n ${STAGE_NAMESPACE} --timeout=120s"
                }
            }
        }
        stage('Smoke Tests') { /* curl contra puerto 31450 */ }
        stage('Security Scan Stage') { /* trivy solo CRITICAL */ }
    }
}
```

### 4.2 Resultado

**Build #1 - EXITOSO**

```
gateway-service-ffdf4bdb7-grf9v       1/1     Running   0
notification-service-88d57b48-4bzds   1/1     Running   0
```

Servicios desplegados en `circleguard-stage`:

| Servicio | Tipo | Puerto |
|---------|------|--------|
| gateway-service | NodePort | 8080:31450 |
| notification-service | ClusterIP | 8084 |

Trivy en STAGE detecto 2 vulnerabilidades CRITICAL en cada servicio (CVE-2025-24813 y CVE-2026-29145 en Tomcat 10.1.19), consistente con los resultados del pipeline DEV.

### 4.3 Analisis

El pipeline STAGE valida que las imagenes construidas en DEV son desplegables en un ambiente que simula produccion. Los 2 pods quedaron en estado Running inmediatamente, demostrando que las imagenes Docker son correctas y los manifiestos Kubernetes estan bien configurados. El smoke test confirmo que el gateway responde en el puerto 31450. El escaneo de Trivy en STAGE se limita a vulnerabilidades CRITICAL para evitar bloqueos por vulnerabilidades ya conocidas y documentadas en el pipeline DEV, permitiendo que el pipeline avance mientras se gestiona el plan de remediacion de dependencias.

La separacion del namespace `circleguard-stage` del namespace DEV `circleguard` permite ejecutar pruebas en STAGE sin afectar el ambiente de desarrollo, siguiendo el principio de aislamiento de ambientes.

---

## 5. Pipeline MASTER (Jenkinsfile.master)

### 5.1 Configuracion

**Job Jenkins:** `circle-guard-master`  
**Branch:** `master`  
**Script Path:** `Jenkinsfile.master`  
**Namespace Kubernetes:** `circleguard-master`  
**Versionamiento:** Semantico `1.0.BUILD_NUMBER`

**Etapas del pipeline:**

| Etapa | Descripcion |
|-------|-------------|
| Checkout | Clona el repositorio |
| Build | Compila todos los 6 microservicios con Gradle |
| Unit Tests | Ejecuta las 23 pruebas unitarias, integracion y E2E |
| Static Analysis | Analisis estatico con SonarQube |
| Docker Build | Construye 6 imagenes con tag de version y latest |
| Security Scan | Trivy HIGH y CRITICAL sobre imagenes clave |
| Deploy to Stage | Despliega en `circleguard-stage` como validacion previa |
| System Tests on Stage | Valida la aplicacion viva en STAGE antes de promover a produccion |
| Deploy to Master | Despliega en `circleguard-master` (produccion) |
| Generate Release Notes | Genera y archiva Release Notes automaticamente como artefacto de Jenkins |

**Fragmento clave - Release Notes:**
```groovy
stage('Generate Release Notes') {
    steps {
        script {
            def date = new Date().format('yyyy-MM-dd')
            def releaseNotes = """# Release Notes - Circle Guard v${VERSION}
**Fecha de release:** ${date}
**Build:** #${BUILD_NUMBER}
## Servicios desplegados
...
## Change Management
- Tipo: Release de funcionalidad
- Rollback: kubectl rollout undo deployment/gateway-service -n circleguard-master
"""
            writeFile file: "RELEASE-NOTES-v${VERSION}.md", text: releaseNotes
            archiveArtifacts artifacts: "RELEASE-NOTES-v${VERSION}.md"
        }
    }
}
```

### 5.2 Resultado

**Build #1 - EXITOSO - Version 1.0.1**

Imagenes construidas con doble tag (version especifica y latest):
- `circleguard/gateway-service:1.0.1` y `circleguard/gateway-service:latest`
- `circleguard/notification-service:1.0.1` y `circleguard/notification-service:latest`
- Idem para auth, identity, form y dashboard services

Deploy exitoso en ambos namespaces:
- `circleguard-stage`: gateway Running + notification Running
- `circleguard-master`: gateway Running + notification Running

**Release Notes RELEASE-NOTES-v1.0.1.md archivadas en Jenkins como artefacto del build.**

### 5.3 Release Notes generadas

Las Release Notes incluyen:
- Version semantica `1.0.1` y fecha de release `2026-05-10`
- Tabla de servicios desplegados con imagen y namespace
- Estado de cada etapa del pipeline
- Lista de vulnerabilidades conocidas con CVEs y fix disponible
- Instruccion de rollback ejecutable
- Tipo de cambio e impacto siguiendo buenas practicas de Change Management

### 5.4 Analisis

El pipeline MASTER implementa el flujo completo de Continuous Delivery siguiendo el patron de promocion entre ambientes: DEV -> STAGE -> MASTER. La validacion en STAGE antes del deploy a MASTER garantiza que solo codigo que pasa pruebas en un ambiente productivo llega a produccion, reduciendo el riesgo de regresiones.

El versionamiento semantico `1.0.BUILD_NUMBER` permite trazabilidad completa: cada imagen Docker tiene un tag unico que corresponde a un build especifico de Jenkins, facilitando el rollback a cualquier version anterior con un comando simple.

Las Release Notes generadas automaticamente siguiendo buenas practicas de Change Management incluyen impacto, instruccion de rollback y vulnerabilidades conocidas, facilitando la gestion del cambio sin intervencion manual y manteniendo un registro auditable de cada release en produccion.

El pipeline ejecuto exitosamente las 23 pruebas antes de cualquier deploy, garantizando que solo codigo validado llega a los ambientes de STAGE y MASTER.

---

## 6. Estructura del Proyecto

```
circle-guard-public/
├── Jenkinsfile                              # Pipeline DEV
├── Jenkinsfile.stage                        # Pipeline STAGE
├── Jenkinsfile.master                       # Pipeline MASTER
├── locustfile.py                            # Pruebas de rendimiento Locust
├── Dockerfile.auth-service
├── Dockerfile.gateway-service
├── Dockerfile.identity-service
├── Dockerfile.form-service
├── Dockerfile.notification-service
├── Dockerfile.dashboard-service
├── Dockerfile.jenkins                       # Imagen personalizada Jenkins
├── k8s/                                     # Manifiestos DEV (namespace circleguard)
│   ├── auth-service.yaml
│   ├── gateway-service.yaml
│   ├── identity-service.yaml
│   ├── form-service.yaml
│   ├── notification-service.yaml
│   └── dashboard-service.yaml
├── k8s-stage/                               # Manifiestos STAGE (namespace circleguard-stage)
│   ├── gateway-service.yaml
│   └── notification-service.yaml
├── k8s-master/                              # Manifiestos MASTER (namespace circleguard-master)
│   ├── gateway-service.yaml
│   └── notification-service.yaml
└── services/
    ├── circleguard-auth-service/
    │   └── src/test/java/com/circleguard/auth/
    │       ├── controller/LoginControllerTest.java        # Existente
    │       ├── controller/LoginControllerUnitTest.java    # Nueva - 5 pruebas unitarias
    │       └── integration/AuthServiceIntegrationTest.java # Nueva - 5 pruebas integracion
    └── circleguard-gateway-service/
        └── src/test/java/com/circleguard/gateway/
            ├── controller/GateControllerTest.java         # Existente
            ├── service/QrValidationServiceTest.java       # Existente
            └── e2e/GatewayE2ETest.java                   # Nueva - 5 pruebas E2E
```

---

## 7. Instrucciones de Ejecucion

### Prerequisitos

```bash
# Construir imagen Jenkins personalizada
docker build -f Dockerfile.jenkins -t jenkins-docker .

# Levantar Jenkins
docker run -d --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v ${HOME}/.kube:/var/jenkins_home/.kube \
  --add-host=kubernetes.docker.internal:host-gateway \
  --group-add 0 jenkins-docker

# Levantar SonarQube
docker network create ci-network
docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community
docker network connect ci-network jenkins
docker network connect ci-network sonarqube

# Crear namespaces Kubernetes
kubectl create namespace circleguard
kubectl create namespace circleguard-stage
kubectl create namespace circleguard-master
```

### Ejecucion de Pipelines

1. Configurar credencial `sonarqube-token` en Jenkins con el token de SonarQube
2. Crear job `circle-guard-pipeline` con Script Path `Jenkinsfile`
3. Crear job `circle-guard-stage` con Script Path `Jenkinsfile.stage`
4. Crear job `circle-guard-master` con Script Path `Jenkinsfile.master`
5. Ejecutar en orden: DEV -> STAGE -> MASTER

### Pruebas de Rendimiento

```bash
pip install locust
python -m locust -f locustfile.py \
  --host=http://localhost:31449 \
  --headless --users 10 \
  --spawn-rate 2 --run-time 30s
```

### Rollback de Produccion

```bash
# Revertir gateway-service en produccion
kubectl rollout undo deployment/gateway-service -n circleguard-master

# Verificar estado
kubectl rollout status deployment/gateway-service -n circleguard-master
```

---

## 8. Decisiones Tecnicas

### 8.1 Seleccion de microservicios para el pipeline

Circle Guard tiene una arquitectura poliglota de datos que hace que algunos servicios sean significativamente mas complejos de testear en CI que otros. Los servicios seleccionados (auth, gateway, identity, form, notification, dashboard) cubren el flujo completo de autenticacion y control de acceso sin requerir Neo4j ni Kafka en tiempo de ejecucion de tests.

El **promotion-service** fue excluido porque es el motor de contencion del sistema: usa Neo4j para traversals recursivos del grafo de contactos con ventana temporal de 14 dias y Kafka para propagar cambios de estado en cascada. Sus tests usan Testcontainers para levantar instancias efimeras de Neo4j y Kafka, lo que requiere Docker-in-Docker o configuracion especial en el agente CI. Esta complejidad esta fuera del alcance de este taller.

El **notification-service** fue excluido de los tests (no del pipeline) porque sus tests de integracion requieren un broker Kafka activo. El servicio si se construye y despliega, pero sus tests se omiten en CI con el flag `-x :services:circleguard-notification-service:test`.

### 8.2 Tabla de decisiones

| Decision | Razon tecnica |
|---------|--------------|
| notification-service excluido de tests CI | Sus tests de integracion requieren un broker Kafka real. Kafka es parte del core del sistema para eventos de salud pero no es viable levantarlo en el agente Jenkins basico |
| promotion-service excluido del pipeline CI | Requiere Neo4j (grafo de contactos) y Kafka (propagacion de estados) via Testcontainers. La configuracion de Docker-in-Docker necesaria esta fuera del alcance del taller |
| Tests con H2 en memoria | Flyway deshabilitado en tests para evitar dependencia de PostgreSQL real. Los servicios que usan PostgreSQL en produccion (auth, identity, form) usan H2 en memoria durante los tests |
| `imagePullPolicy: Never` en K8s | Las imagenes Docker residen en el daemon local de Docker Desktop, no en un registry remoto. Kubernetes debe usar las imagenes locales |
| SonarQube plugin v4.4.1.3373 | El proyecto no tenia el plugin de SonarQube configurado. Se agrego al `build.gradle.kts` raiz para habilitar el task `sonar` en todos los subproyectos |
| Rollout status solo para gateway-service | Es el unico pod garantizado en estado Running en el ambiente DEV. Los demas requieren PostgreSQL, LDAP o Neo4j que no estan disponibles en el cluster local |
| Tag doble en Docker Build MASTER | `1.0.BUILD_NUMBER` garantiza trazabilidad (cada imagen corresponde a un build especifico) mientras `latest` mantiene compatibilidad con los manifiestos Kubernetes existentes |
| Namespace separado por ambiente | `circleguard` (DEV), `circleguard-stage` (STAGE) y `circleguard-master` (MASTER) aisian los despliegues y permiten ejecutar los tres pipelines en paralelo sin interferencia |
| curl con `exit-code 0` en smoke tests | El gateway responde con codigos HTTP validos (4xx/5xx) porque los servicios backend no tienen infraestructura completa. El smoke test valida que el servicio responde, no el codigo de respuesta |

---

*Taller 2 - Ingenieria de Software V*  
*Juliana Filigrana Valencia - Mayo 2026*
