# Informe Final — Circle Guard

**Control de Acceso Sanitario Universitario**

| Campo | Datos |
|-------|-------|
| **Estudiante** | Juliana Filigrana Valencia - Juan Manuek Casanova Marin|
| **Curso** | Ingeniería de Software V — Semestre 8 |
| **Institución** | Universidad ICESI |
| **Fecha** | Junio 2026 |
| **Repositorio** | https://github.com/JuliianaV2106/circle-guard-public |
| **Kanban** | https://github.com/users/JuliianaV2106/projects/2 |

---

## Resumen Ejecutivo

Circle Guard es un sistema de control de acceso sanitario universitario diseñado para identificar círculos de contacto entre estudiantes y aplicar cercos sanitarios rápidos, preservando la privacidad mediante anonimización de identidades. El sistema está compuesto por 8 microservicios desarrollados en Spring Boot 3.2 con Kotlin, desplegados en Kubernetes sobre Docker Desktop, con infraestructura definida como código via Terraform, pipelines CI/CD con Jenkins, monitoreo con Prometheus/Grafana/Jaeger/ELK, y seguridad multicapa.

---

## 1. Metodología Ágil (5%)

### Framework: Kanban + GitFlow

- **Kanban**: GitHub Projects con 16 historias de usuario organizadas en 5 épicas
- **GitFlow**: Ramas `develop`, `master`, `release/*`, `feature/*`
- **Sprints**: 3 sprints planificados
  - Sprint 1: 32 puntos cerrados
  - Sprint 2: 47 puntos cerrados
  - Sprint 3: Planeado

### Historias de Usuario

| Épica | HUs | Estado |
|-------|-----|--------|
| Infraestructura | INFRA-1, INFRA-2, INFRA-3, INFRA-4 | ✅ |
| Patrones | PAT-1, PAT-2, PAT-3 | ✅ |
| CI/CD | CICD-1, CICD-2, CICD-3, CICD-4 | ✅ |
| Pruebas | TEST-1, TEST-2 | ✅ |
| Seguridad | SEC-1, SEC-2 | ✅ |

### Documentación
- `docsagil/metodologia.md`
- `docsagil/hu-*.md` (6 archivos de historias de usuario)

---

## 2. Infraestructura como Código (15%)

### Tecnología: Terraform 1.6+

| Ambiente | Namespace K8s | Servicios | Pipeline Asociado |
|----------|---------------|-----------|-------------------|
| DEV | `circleguard` | 6 servicios | Jenkinsfile |
| STAGE | `circleguard-stage` | 2 servicios | Jenkinsfile.stage |
| MASTER | `circleguard-master` | 2 servicios | Jenkinsfile.master |

### Módulos Terraform (5 módulos)

| Módulo | Propósito |
|--------|-----------|
| `namespace` | Namespace de Kubernetes con labels |
| `configmap` | ConfigMap con variables de entorno |
| `microservice` | Deployment + Service (con health probes opcionales) |
| `secrets` | Kubernetes Secrets para datos sensibles |
| `rbac` | ServiceAccount + Role + RoleBinding por servicio |

### Características
- Estado remoto en Terraform Cloud
- Variables sensibles marcadas como `sensitive = true`
- Health probes (liveness + readiness) en todos los servicios
- ServiceAccount individual por microservicio

### Documentación
- `docsinfraestructura/index.md`
- `terraform/modules/` (5 módulos)
- `terraform/environments/` (3 ambientes)

---

## 3. Patrones de Diseño (10%)

| Patrón | Librería | Servicio | Descripción |
|--------|----------|----------|-------------|
| Circuit Breaker | Resilience4j | Auth Service | Corta llamadas a identity-service cuando falla, con fallback |
| Retry | Resilience4j | Auth Service | Reintenta hasta 3 veces con espera de 1s |
| Time Limiter | Resilience4j | Auth Service | Timeout de 3s en llamadas a identity-service |
| External Configuration | K8s ConfigMap | Todos | Configuración centralizada por ambiente |
| Attribute Converter | JPA `@Convert` | Identity Service | Encriptación de identidades en reposo |

### Documentación
- `docspatrones/index.md`
- `docspatrones/patron-circuit-breaker.md`
- `docspatrones/patron-retry.md`
- `docspatrones/patron-external-config.md`
- `docspatrones/patron-health-check.md`

---

## 4. CI/CD Avanzado (15%)

### Pipeline DEV (Jenkinsfile)

```
Checkout → Build → Unit Tests → Dependency Check (SCA)
→ SonarQube (SAST) → Docker Build → Trivy (Container)
→ OWASP ZAP (DAST) → Email Notification
```

### Pipeline STAGE (Jenkinsfile.stage)

```
Checkout → Verify Images → Terraform Apply → Health Check
→ Smoke Tests → Trivy → OWASP ZAP → Email
```

### Pipeline MASTER (Jenkinsfile.master)

```
Checkout → Semantic Versioning → Build → Tests → Dependency Check
→ SonarQube → Docker Build → Trivy → ZAP → Deploy Stage
→ System Tests → Aprobación Manual → Deploy Master
→ Generate Release Notes → Tag Release → GitHub Release → Email
```

### Características Clave

- **Versionado semántico**: Auto-incremento PATCH desde último tag
- **Release Notes automáticas**: Script `scripts/generate-release-notes.sh`
- **GitHub Releases**: Creación automática via `gh` CLI
- **Notificaciones email**: Éxito/fallo del pipeline
- **Triggers automáticos**: `pollSCM('*/5 * * * *')`

### Documentación
- `README-CICD.md`
- `docsrelease-notes/process.md`

---

## 5. Pruebas Completas (15%)

| Tipo | Herramienta | Cobertura/Resultado |
|------|-------------|---------------------|
| Unitarias | JUnit 5 + Mockito | ≥70% (verificado con JaCoCo) |
| Integración | Testcontainers | PostgreSQL, Neo4j, Kafka embebidos |
| SAST | SonarQube | Analysis Gate superado |
| SCA | OWASP Dependency-Check | Reporte HTML generado |
| Seguridad (Container) | Trivy | HIGH/CRITICAL escaneados |
| Seguridad (DAST) | OWASP ZAP | Baseline scan contra gateway |
| Rendimiento | Locust (locustfile.py) | Pruebas de carga |

### Resultados
- Auth Service: LoginController 100% cubierto (login valido, credenciales invalidas, request malformado, anonymousId)
- JaCoCo: Umbral 70% configurado como quality gate
- Trivy: --timeout 10m, severity HIGH/CRITICAL
- OWASP ZAP: No bloqueante (exit-code 0)

### Documentación
- `docspruebas/index.md`
- `docspruebas/pruebas-unitarias.md`
- `docspruebas/pruebas-integracion.md`
- `docspruebas/pruebas-seguridad.md`
- `docspruebas/pruebas-rendimiento.md`

---

## 6. Change Management y Release Notes (5%)

### Proceso Formal de Change Management

| Tipo de Cambio | Descripción | Aprobación |
|----------------|-------------|------------|
| Normal | Nuevas funcionalidades, cambios mayores | CAB semanal |
| Estándar | Bugfix, config, dependencias | Pipeline CI/CD |
| Emergencia | Hotfix seguridad, caída de servicio | Líder técnico + PO |
| Cosmético | Docs, logging, sin efecto runtime | Ninguna |

### Change Advisory Board (CAB)

| Rol | Responsabilidad |
|-----|----------------|
| Product Owner | Prioriza cambios, decide business impact |
| Líder Técnico | Evalúa riesgo técnico y plan de rollback |
| DevOps | Revisa impacto en infraestructura |
| QA | Valida plan de pruebas |

### Release Notes Automáticas

Generadas por `scripts/generate-release-notes.sh` con:
- Commits agrupados por tipo (feat, fix, docs, refactor, chore)
- Servicios desplegados con imágenes Docker
- Plan de rollback
- CRQ ID y metadatos de Change Management

### Documentación
- `docschange-management/index.md`
- `docsrelease-notes/process.md`

---

## 7. Observabilidad y Monitoreo (10%)

### Stack Implementado

| Componente | Propósito | Puerto | Imagen |
|-----------|-----------|--------|--------|
| Prometheus | Métricas y alertas | 9090 | prom/prometheus:v2.53.0 |
| Grafana | Dashboards | 3001 | grafana/grafana:11.0.0 |
| Jaeger | Tracing distribuido | 16686 | jaegertracing/all-in-one:1.58 |
| Elasticsearch | Almacenamiento de logs | 9200 | elastic/elasticsearch:8.11.0 |
| Logstash | Procesamiento de logs | 5044 | logstash:8.11.0 |
| Kibana | Visualización de logs | 5601 | kibana/kibana:8.11.0 |

### Dashboards de Grafana (3)

| Dashboard | UID | Contenido |
|-----------|-----|-----------|
| Service Health | circleguard-services | Estado UP/DOWN, tasa requests, errores HTTP, latencia P95, CPU |
| JVM Metrics | circleguard-jvm | Heap/non-heap memory, GC pauses, threads, clases cargadas |
| Business Metrics | circleguard-business | Usuarios activos, QR generados, notificaciones, verificaciones |

### Métricas de Negocio

Se implementaron 6 métricas personalizadas via Micrometer `MeterRegistry` en un `BusinessMetricsService` para el dashboard de negocio:

| Métrica | Tipo | Descripción |
|---------|------|-------------|
| `circleguard_active_users_total` | Gauge | Usuarios activos concurrentes |
| `circleguard_qr_codes_generated_total` | Counter | Total de códigos QR generados |
| `circleguard_notifications_sent_total` | Counter | Notificaciones enviadas |
| `circleguard_identity_verifications_total` | Counter | Verificaciones de identidad |
| `circleguard_form_submissions_total` | Counter | Formularios de salud enviados |
| `circleguard_kafka_messages_total` | Counter | Mensajes en Kafka |

Las métricas se simulan con tareas programadas (`@Scheduled`) cada 3-15 segundos para propósitos de demostración, pero en producción serían alimentadas por eventos reales de los microservicios.

### Alertas Prometheus (5 reglas)

| Alerta | Condición | Severidad |
|--------|-----------|-----------|
| ServiceDown | up == 0 por 1m | CRITICAL |
| HighErrorRate | 5xx > 5% por 2m | CRITICAL |
| HighResponseTime | P95 > 5s por 5m | WARNING |
| HighMemoryUsage | Heap > 85% por 5m | WARNING |
| HighCpuUsage | CPU > 80% por 5m | WARNING |

### Health Checks

Cada servicio expone:
- `/actuator/health` — Health check general
- `/actuator/health/liveness` — Liveness probe (K8s)
- `/actuator/health/readiness` — Readiness probe (K8s)
- `/actuator/prometheus` — Métricas para Prometheus

### Documentación
- `docsoperaciones/observabilidad.md`
- `config/prometheus/`
- `config/grafana/`
- `config/elasticsearch/`
- `config/logstash/`
- `config/kibana/`

---

## 8. Seguridad (5%)

### Escaneo Continuo de Vulnerabilidades

| Herramienta | Tipo | Pipeline |
|------------|------|----------|
| SonarQube | SAST | DEV, MASTER |
| OWASP Dependency-Check | SCA | DEV, MASTER |
| Trivy | Container Scan | DEV, STAGE, MASTER |
| OWASP ZAP | DAST | DEV, STAGE, MASTER |

### Gestión Segura de Secretos

```
application.yml (valores por defecto DEV)
       ↓
  Kubernetes Secret (circleguard-secrets)
       ↓
  env_from → Deployment (sobrescribe por env vars)
       ↓
  Spring Boot (environment properties > YAML)
```

### RBAC en Kubernetes

| Servicio | ServiceAccount | Permisos |
|----------|---------------|----------|
| auth-service | auth-service-sa | get/list/watch pods, services, endpoints |
| gateway-service | gateway-service-sa | get/list/watch pods, services, endpoints |
| identity-service | identity-service-sa | get/list/watch pods, services, endpoints |
| form-service | form-service-sa | get/list/watch pods, services, endpoints |
| notification-service | notification-service-sa | get/list/watch pods, services, endpoints |
| dashboard-service | dashboard-service-sa | get/list/watch pods, services, endpoints |

### TLS para Servicios Expuestos

- Script `scripts/gen-certs.sh` para certificados autofirmados
- Ingress con TLS termination para gateway
- Documentación de cert-manager + Let's Encrypt para producción

### Documentación
- `docsseguridad/index.md`
- `scripts/gen-certs.sh`
- `scripts/init-secrets.sh`
- `terraform/modules/secrets/`
- `terraform/modules/rbac/`

---

## 9. Documentación y Presentación (10%)

### Documentación del Proyecto

| Documento | Descripción |
|-----------|-------------|
| `docs/README.md` | Índice central de documentación |
| `docs/git-organizacion.md` | Estrategia de branching y commits |
| `docs/costos-infraestructura.md` | Análisis de costos |
| `docs/video-demo.md` | Guion para video demostrativo |
| `docs/presentacion.md` | Presentación de 17 diapositivas |
| `docsoperaciones/manual-operaciones.md` | Manual de operaciones completo |

### Repositorio Git

- 58 commits, 4 ramas locales, 5 remotas
- Conventional Commits (feat, fix, docs, chore, ci)
- GitFlow: develop → master, release/1.0.0
- Tags de release generados automáticamente

### Costos de Infraestructura

| Escenario | Costo/mes |
|-----------|-----------|
| Desarrollo local (Docker Desktop) | $0 USD |
| Producción en nube (EKS/GKE) | ~$240 USD |
| Optimizado (spot + auto-hospedado) | ~$135 USD |

---

## Arquitectura Técnica

### Microservicios

| # | Servicio | Puerto | Base de Datos | Tech Stack |
|---|----------|--------|---------------|------------|
| 1 | Auth Service | 8180 | PostgreSQL + LDAP | Spring Boot, JPA, Security, JWT, Resilience4j |
| 2 | Identity Service | 8083 | PostgreSQL | Spring Boot, JPA, Kafka, Flyway |
| 3 | Promotion Service | 8088 | PostgreSQL + Neo4j + Redis | Spring Boot, JPA, Neo4j, Kafka, Cache |
| 4 | Notification Service | 8082 | Kafka | Spring Boot, Kafka, Mail, Twilio |
| 5 | Form Service | 8086 | PostgreSQL + Kafka | Spring Boot, JPA, Kafka, Flyway |
| 6 | Dashboard Service | 8084 | PostgreSQL | Spring Boot, JPA, Flyway |
| 7 | File Service | 8085 | — | Spring Boot, placeholder S3 |
| 8 | Gateway Service | 8087 | Redis | Spring Boot, Redis, JWT, QR |

### Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 3.2.4, Kotlin, Java 21 |
| Build | Gradle 8.7, JaCoCo, SonarQube, OWASP Dependency-Check |
| Bases de datos | PostgreSQL 16, Neo4j 5.26, Redis 7.2 |
| Mensajería | Apache Kafka 7.6 |
| Contenedores | Docker, Docker Compose |
| Orquestación | Kubernetes (Docker Desktop) |
| IaC | Terraform 1.6+ |
| CI/CD | Jenkins (3 pipelines) |
| Monitoreo | Prometheus, Grafana, Jaeger, ELK |
| Seguridad | Trivy, OWASP ZAP, OWASP Dependency-Check |
| Autenticación | LDAP + JWT HMAC-SHA256, BCrypt |
| Frontend | Expo (React Native) |

---

## Resultados y Métricas

### Pipeline CI/CD

| Pipeline | Estado | Última Ejecución |
|----------|--------|-----------------|
| DEV | ✅ Exitoso | Build + Test + Sonar + Docker + Trivy + ZAP |
| STAGE | ✅ Exitoso | Terraform + Smoke Tests + Trivy + ZAP |
| MASTER | ⚠️ Trivy timeout | Build + Test + Docker + Sonar exitosos |

### Pruebas

| Servicio | Tests | Estado |
|----------|-------|--------|
| Auth Service | 0 failures, 0 errors | ✅ |
| Dashboard Service | 0 failures, 0 errors | ✅ |
| Form Service | 0 failures, 0 errors | ✅ |
| Gateway Service | 0 failures, 0 errors | ✅ |
| Identity Service | 0 failures, 0 errors | ✅ |
| Notification Service | 0 failures, 0 errors | ✅ |
| Promotion Service | 0 failures, 0 errors | ✅ |
| File Service | Sin tests | — |

### Monitoreo

- Prometheus: 8 targets configurados (todos los servicios)
- Grafana: 3 dashboards pre-configurados
- Jaeger: Tracing distribuido via Zipkin API
- ELK: Logs centralizados

---

## Lecciones Aprendidas

1. **Terraform + K8s**: La curva de aprendizaje es alta pero el resultado (declaratividad, reproducibilidad) vale la pena
2. **MockBean en tests**: Diferencia entre `KafkaTemplate<String,Object>` y `KafkaTemplate<String,String>` causó errores de compilación difíciles de depurar
3. **Trivy timeout**: La descarga inicial de la base de datos de Java (878 MB) excede el timeout por defecto — solución: `--timeout 10m`
4. **Monitorización desde el inicio**: Agregar Actuator y métricas desde el día 1 simplifica enormemente la depuración
5. **Secretos nunca en YAML**: Los valores hardcodeados en `application.yml` son un riesgo de seguridad — usar siempre K8s Secrets
6. **Kotlin DSL en Gradle**: La sintaxis `tasks.jacocoTestReport` no funciona en Kotlin DSL; usar `tasks.withType<JacocoReport>()`

---

## Conclusiones

1. Circle Guard cumple con todos los 9 requisitos funcionales y no funcionales del curso
2. La arquitectura de microservicios permite escalar componentes individualmente según demanda
3. La privacidad y la velocidad de contención no son mutuamente excluyentes — el sistema demuestra ambas
4. El pipeline CI/CD automatiza completamente el ciclo de vida: build → test → scan → deploy → release
5. El stack de monitoreo (Prometheus + Grafana + Jaeger + ELK) proporciona visibilidad completa del sistema
6. La infraestructura como código (Terraform) garantiza reproducibilidad entre ambientes
7. El sistema está preparado para despliegue en producción con costos estimados de ~$240/mes

---

## Referencias

- **Repositorio**: https://github.com/JuliianaV2106/circle-guard-public
- **Kanban**: https://github.com/users/JuliianaV2106/projects/2
- **Jenkins**: http://localhost:8080
- **Grafana**: http://localhost:3001 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686
- **Kibana**: http://localhost:5601
- **SonarQube**: http://localhost:9000

---

## Apéndice A: Estructura del Repositorio

```
circle-guard-public/
├── build.gradle.kts          # Build system (Gradle 8.7)
├── settings.gradle.kts       # Módulos del proyecto
├── version.properties        # Versión semántica
├── Jenkinsfile               # Pipeline DEV
├── Jenkinsfile.stage         # Pipeline STAGE
├── Jenkinsfile.master        # Pipeline MASTER
├── docker-compose.dev.yml    # Infraestructura local
├── docker-compose.full.yml   # Stack completo + monitoreo
├── Dockerfile.*              # Dockerfiles (7)
├── services/                 # 8 microservicios
│   ├── circleguard-auth-service/
│   ├── circleguard-gateway-service/
│   ├── circleguard-identity-service/
│   ├── circleguard-form-service/
│   ├── circleguard-notification-service/
│   ├── circleguard-dashboard-service/
│   ├── circleguard-file-service/
│   └── circleguard-promotion-service/
├── terraform/                # IaC (3 ambientes, 5 módulos)
├── k8s/                      # Manifiestos K8s DEV
├── k8s-stage/                # Manifiestos K8s STAGE
├── k8s-master/               # Manifiestos K8s MASTER
├── config/                   # Monitoreo (Prometheus, Grafana, ELK)
├── scripts/                  # Scripts (4)
├── mobile/                   # App Expo (React Native)
├── docs/                     # Documentación central
├── docsagil/                 # Metodología ágil
├── docsinfraestructura/      # Infraestructura
├── docspatrones/             # Patrones de diseño
├── docspruebas/              # Pruebas
├── docsrelease-notes/        # Release notes
├── docschange-management/    # Change management
├── docsoperaciones/          # Operaciones y monitoreo
└── docsseguridad/            # Seguridad
```

## Apéndice B: Comandos Útiles

```bash
# Iniciar todo el stack
docker compose -f docker-compose.full.yml up -d

# Iniciar solo monitoreo
docker compose -f docker-compose.full.yml up -d prometheus grafana jaeger

# Ejecutar tests
./gradlew test --no-daemon

# Ver health de un servicio
curl http://localhost:8087/actuator/health

# Ver liveness/readiness probes
curl http://localhost:8087/actuator/health/liveness
curl http://localhost:8087/actuator/health/readiness

# Ver métricas Prometheus
curl http://localhost:8087/actuator/prometheus

# Ver métricas de negocio en Prometheus
curl http://localhost:9090/api/v1/query?query=circleguard_qr_codes_generated_total

# Desplegar con Terraform
cd terraform/environments/dev
terraform init
terraform apply -auto-approve

# Generar release notes
./scripts/generate-release-notes.sh 1.0.0

# Inicializar secretos en K8s
./scripts/init-secrets.sh circleguard

# Generar certificados TLS
./scripts/gen-certs.sh api.circleguard.edu

# Fix DNS para Docker BuildKit (si falla resolución de registry)
# Agregar a %USERPROFILE%\.docker\daemon.json:
# { "dns": ["8.8.8.8", "1.1.1.1"] }
# Luego reiniciar Docker Desktop

# Pre-pull de imágenes base para builds Jenkins
docker pull gradle:8.7-jdk21
docker pull eclipse-temurin:21-jre-alpine
```

---

* Junio 2026 — Ingeniería de Software V*
