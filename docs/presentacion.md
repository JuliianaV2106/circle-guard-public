# Presentación del Proyecto — Circle Guard

**Duración estimada:** 20-25 minutos
**Formato:** PowerPoint / Google Slides / Canva
**Idioma:** Español

---

## Slide 1: Portada

- Logo de Circle Guard
- Título: "Circle Guard — Control de Acceso Sanitario Universitario"
- Subtítulo: "Ingeniería de Software V — Semestre 8"
- Autora: Juliana Filigrana Valencia
- Fecha: Junio 2026
- Repositorio: https://github.com/JuliianaV2106/circle-guard-public

---

## Slide 2: Problema

**Contexto real:**
- 1 estudiante infectado puede exponer a 50+ contactos en 24h
- Rastreo manual toma días → brotes se expanden
- Datos personales de salud son altamente sensibles (Ley 1581 de 2012 en Colombia)

**Pregunta clave:** ¿Cómo contener brotes en < 60 segundos sin exponer identidades?

**Métrica objetivo:**
- Tiempo de contención: < 60 segundos
- Protección de identidad: anonimización irreversible
- Sin almacenamiento de datos biométricos ni de ubicación

---

## Slide 3: Solución — Circle Guard

**Diagrama de flujo completo:**

```
[Estudiante] → Escanea QR → [Gateway Service :8087]
       → Valida token JWT en Redis
       → [Auth Service :8180] → Autenticación LDAP + PostgreSQL
           → [Identity Service :8083] → Anonimización irreversible (SHA-256 + UUID)
               → [Graph DB : Neo4j] → Construcción de círculos de contacto
                   → [Promotion Service :8088] → Motor de promoción de estado
                       → [Notification Service :8082] → Email + SMS + Push
                           → [Dashboard Service :8084] → Visualización anonimizada
```

**3 pilares:**
1. **Privacidad como código** — Anonimización irreversible desde el Identity Service
2. **Contención recursiva** — Promoción de estado en Neo4j en milisegundos
3. **Integración universitaria** — LDAP existente, horarios, puntos de acceso WiFi

---

## Slide 4: Arquitectura Técnica

**8 microservicios:**

| Servicio | Puerto | BD | Lenguaje | Rol |
|----------|--------|-----|----------|-----|
| Auth Service | 8180 | PostgreSQL + LDAP | Java 21 | Autenticación dual (JWT + LDAP) |
| Gateway Service | 8087 | Redis | Java 21 | Validación QR, rate limiting |
| Identity Service | 8083 | PostgreSQL | Java 21 | Vault de identidades anonimizadas |
| Form Service | 8086 | PostgreSQL + Kafka | Java 21 | Encuestas de salud |
| Notification Service | 8082 | Kafka | Java 21 | Email/SMS/Push |
| Dashboard Service | 8084 | PostgreSQL | Java 21 | Dashboard público anonimizado |
| File Service | 8085 | — | Java 21 | Subida de documentos |
| Promotion Service | 8088 | PostgreSQL + Neo4j + Redis | Java 21 | Motor de promoción de estado |

**Infraestructura base:**
- PostgreSQL 16, Neo4j 5, Kafka 3.6, Redis 7, OpenLDAP
- Todos corriendo en Docker Desktop + Kubernetes

---

## Slide 5: Metodología Ágil (5%)

**Framework:** Kanban + GitFlow

**GitFlow implementado:**
- `develop` → Integración continua (pipelines DEV)
- `master` → Releases a STAGE y MASTER
- `release/*` → Preparación de releases
- Commits con **Conventional Commits**: `feat:`, `fix:`, `docs:`, `ci:`, etc.

**Kanban en GitHub Projects:**
- https://github.com/users/JuliianaV2106/projects/2
- 16 historias de usuario en 5 épicas
- Columnas: To Do, In Progress, Done

**Sprints:**
| Sprint | Puntos | HUs cerradas |
|--------|--------|-------------|
| Sprint 1 | 32 pts | INFRA-1 a INFRA-4, PAT-1 a PAT-3 |
| Sprint 2 | 47 pts | CICD-1 a CICD-4, TEST-1, TEST-2, SEC-1, SEC-2 |
| Sprint 3 | Planeado | Mejoras, deuda técnica |

**Documentación:** `docsagil/metodologia.md`, `docsagil/hu-*.md` (6 archivos)

---

## Slide 6: Infraestructura como Código (15%)

**Herramienta:** Terraform v1.14.6

**5 módulos reutilizables:**

| Módulo | Recurso principal | Variables clave |
|--------|------------------|----------------|
| `namespace` | `kubernetes_namespace` | `name`, `labels` |
| `configmap` | `kubernetes_config_map` | `service_name`, `spring_profile`, `log_level` |
| `microservice` | `kubernetes_deployment` + `kubernetes_service` | `replicas`, `container_port`, `probe_path`, `node_port` |
| `secrets` | `kubernetes_secret` | `secret_name`, `secret_data` (sensitive) |
| `rbac` | `ServiceAccount` + `Role` + `RoleBinding` | `service_account_name`, `namespace`, `rules` |

**3 ambientes independientes:**

| Ambiente | Namespace K8s | Perfil Spring | Propósito |
|----------|--------------|---------------|-----------|
| DEV | `circleguard` | `dev` | Desarrollo, tests automáticos |
| STAGE | `circleguard-stage` | `stage` | Pre-producción, smoke tests |
| MASTER | `circleguard-master` | `prod` | Producción real |

**Estado remoto:** Terraform Cloud (workspaces: `circle-guard-dev`, `circle-guard-stage`, `circle-guard-master`)

**Ejemplo de uso del módulo microservice en `environments/dev/main.tf`:**
```hcl
module "gateway_service" {
  source                 = "../../modules/microservice"
  service_name           = "gateway-service"
  namespace_name         = kubernetes_namespace.this.metadata[0].name
  container_port         = 8087
  node_port              = 31449
  service_type           = "NodePort"
  spring_profile         = "dev"
  replicas               = 1
  enable_liveness_probe  = true
  enable_readiness_probe = true
  probe_path             = "/actuator/health"
}
```

**Resultado en K8s:**
```bash
kubectl get pods -n circleguard
# gateway-service-xxx  1/1  Running
# auth-service-xxx     1/1  Running
# ...
```

---

## Slide 7: Patrones de Diseño (10%)

**9 patrones documentados en `docspatrones/`:**

| # | Patrón | Tipo | Implementación | Archivo |
|---|--------|------|---------------|---------|
| 1 | API Gateway | Arquitectura | Gateway Service como punto único de entrada | Existente |
| 2 | Strangler Fig | Arquitectura | Migración gradual de monolito a microservicios | Existente |
| 3 | Database per Service | Datos | Cada servicio con su propia BD | Existente |
| 4 | Event-Driven | Mensajería | Kafka para comunicación asíncrona | Existente |
| 5 | Anonymization | Privacidad | SHA-256 + UUID en Identity Service | Existente |
| 6 | **Circuit Breaker** | Resiliencia | `@CircuitBreaker` en `IdentityClient.java` | ✅ Nuevo |
| 7 | **External Configuration** | Configuración | ConfigMaps + Terraform por ambiente | ✅ Nuevo |
| 8 | **Health Check** | Disponibilidad | Actuator + K8s probes en todos los servicios | ✅ Nuevo |
| 9 | **Retry** | Resiliencia | `@Retry` (Resilience4j) + Spring Retry | ✅ Nuevo |

**Código del Circuit Breaker + Retry en `IdentityClient.java`:**
```java
@Retry(name = "identityService", fallbackMethod = "getAnonymousIdFallback")
@CircuitBreaker(name = "identityService", fallbackMethod = "getAnonymousIdFallback")
public UUID getAnonymousId(String realIdentity) { ... }

public UUID getAnonymousIdFallback(String realIdentity, Exception ex) {
    return UUID.nameUUIDFromBytes(realIdentity.getBytes());
}
```

**Configuración Resilience4j (application.yml):**
```yaml
resilience4j.circuitbreaker:
  instances:
    identityService:
      sliding-window-size: 10
      failure-rate-threshold: 50
      wait-duration-in-open-state: 10s

resilience4j.retry:
  instances:
    identityService:
      max-attempts: 3
      wait-duration: 1s
```

---

## Slide 8: CI/CD Avanzado (15%)

**3 pipelines Jenkins completos:**

**Pipeline DEV (`Jenkinsfile`) — Trigger: push a develop:**
```
pollSCM('*/5 * * * *')
  → Checkout → Build (gradlew clean build -x test)
  → Test (JUnit 5 + Mockito, 51 tests)
  → Coverage Report (JaCoCo ≥70%)
  → Docker Build (8 imágenes)
  → Dependency Check (OWASP SCA)
  → SonarQube Analysis (Quality Gate)
  → Trivy Security Scan (HIGH/CRITICAL)
  → OWASP ZAP Scan (DAST)
  → Email (success/failure)
```

**Pipeline STAGE (`Jenkinsfile.stage`) — Trigger: push a master:**
```
  → Terraform init/apply (stage.tfvars)
  → Smoke tests
  → ZAP Scan + Trivy
  → Email
```

**Pipeline MASTER (`Jenkinsfile.master`) — Trigger: push a master:**
```
  → Semantic Versioning (auto-increment PATCH)
  → Build + Test + Coverage (ídem DEV)
  → Dependency Check + SonarQube
  → Docker Build + Trivy + ZAP
  → Deploy to STAGE (Terraform)
  → System Tests
  → ⏸️ APPROVAL (input manual, timeout 30 min)
  → Deploy to MASTER (Terraform)
  → Generate Release Notes
  → Tag Release (git tag v1.0.X)
  → GitHub Release (gh release create)
  → Email
```

**Aprobación manual para producción (`Jenkinsfile.master:171-186`):**
```groovy
stage('Approval') {
    steps {
        timeout(time: 30, unit: 'MINUTES') {
            input message: 'Aprobar despliegue a MASTER?',
                ok: 'Aprobar',
                submitter: 'admin'
        }
    }
}
```

**Notificaciones por email (`Jenkinsfile:post`):**
```groovy
post {
    success { emailext(subject: "SUCCESS...", to: 'juliianavalenciia21@gmail.com') }
    failure { emailext(subject: "FAILED...", to: '${DEFAULT_RECIPIENTS}') }
}
```

**SonarQube:** http://localhost:9000 — Quality Gate Passed, cobertura ≥70%
**Trivy:** Escanea vulnerabilidades en imágenes Docker, timeout 10m
**Versionado semántico:** `git describe --tags` → extrae MAJOR.MINOR.PATCH → incrementa PATCH

---

## Slide 9: Pruebas Completas (15%)

| Tipo | Herramienta | Pipeline Stage | Resultado |
|------|-------------|---------------|-----------|
| Unitarias | JUnit 5 + Mockito | `Test` | 51 tests, todos pasan |
| Integración | Spring Boot Test | `Test` | auth ↔ identity |
| Cobertura | JaCoCo | `Coverage Report` | ≥70% en todos los servicios |
| SAST | SonarQube | `SonarQube Analysis` | Quality Gate: Passed |
| SCA | OWASP Dependency-Check | `Dependency Check (SCA)` | No bloqueante (failBuildOnCVSS=11) |
| Container | Trivy | `Trivy Security Scan` | HIGH/CRITICAL |
| DAST | OWASP ZAP | `OWASP ZAP Scan` | Gateway API scan |
| Rendimiento | Locust | Manual | `locustfile.py` |

**JaCoCo config (`build.gradle.kts:72-80`):**
```kotlin
tasks.withType<JacocoCoverageVerification> {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}
```

**Locust (`locustfile.py`):**
```python
class GatewayUser(HttpUser):
    wait_time = between(1, 3)
    @task(3)  def validate_valid_token(self)
    @task(2)  def validate_invalid_token(self)
    @task(1)  def validate_empty_token(self)
```

**Documentación:** `docspruebas/` (5 archivos: index, unitarias, integración, rendimiento, seguridad)

---

## Slide 10: Change Management (5%)

**1. Proceso formal de Change Management (`docschange-management/index.md`):**

| Tipo de Cambio | Ejemplo | Aprobación | Ventana |
|---------------|---------|-----------|---------|
| **Normal** | Nueva funcionalidad, cambio de API | CAB (reunión semanal) | Sprint planning |
| **Estándar** | Bug fix, config change | Pipeline CI/CD automático | Cualquier día hábil |
| **Emergencia** | Hotfix de seguridad | Líder técnico + PO | Inmediata, post-mortem |
| **Cosmético** | Documentación, logging | Ninguna | Cualquier momento |

**Change Advisory Board (CAB):**
| Rol | Responsabilidad |
|-----|----------------|
| Product Owner | Prioriza cambios, decide business impact |
| Líder Técnico | Evalúa riesgo técnico y plan de rollback |
| DevOps | Revisa impacto en infraestructura |
| QA | Valida plan de pruebas |

**Template de Change Request:**
```markdown
---
título: "[CRQ-XXX] Descripción del cambio"
tipo: normal | estandar | emergencia | cosmetico
riesgo: alto | medio | bajo
servicios_afectados: [lista]
plan_rollback: kubectl rollout undo deployment/...
---
```

**2. Release Notes automáticas (`scripts/generate-release-notes.sh`):**
```bash
# Uso:
./scripts/generate-release-notes.sh 1.0.5 RELEASE-NOTES-v1.0.5.md
# Genera:
#   - Agrupa commits por conventional commit type
#   - Lista servicios desplegados con sus versiones
#   - Incluye plan de rollback (kubectl rollout undo)
#   - Incluye información de Change Management (CRQ ID)
```

**3. Etiquetado de releases (`Jenkinsfile.master:211-237`):**
```groovy
stage('Tag Release') {
    sh "git tag -a v${VERSION} -m 'Release v${VERSION}'"
    sh "git push origin v${VERSION}"
}
stage('GitHub Release') {
    sh "gh release create v${VERSION} --notes-file RELEASE-NOTES-v${VERSION}.md"
}
```

**4. Plan de rollback (incluido en cada release notes):**
```bash
# Revertir al deployment anterior:
kubectl rollout undo deployment/gateway-service -n circleguard-master
# O desplegar versión específica:
kubectl set image deployment/gateway-service \
  gateway-service=circleguard/gateway-service:v1.0.4 \
  -n circleguard-master
```

---

## Slide 11: Observabilidad (10%)

**Stack completo en `docker-compose.full.yml`:**

| Componente | Puerto | Propósito | Imagen |
|-----------|--------|-----------|--------|
| **Prometheus** | 9090 | Métricas de 8 microservicios | prom/prometheus:v2.53.0 |
| **Grafana** | 3001 | Dashboards + alertas | grafana/grafana:11.0.0 |
| **Jaeger** | 16686 | Tracing distribuido (Zipkin API) | jaegertracing/all-in-one:1.58 |
| **Elasticsearch** | 9200 | Almacenamiento de logs | elastic/elasticsearch:8.11.0 |
| **Logstash** | 5000/5044 | Procesamiento de logs | logstash:8.11.0 |
| **Kibana** | 5601 | Visualización de logs | kibana:8.11.0 |

**Endpoints Actuator en cada servicio:**
```bash
curl localhost:8087/actuator/health
# → {"status":"UP","components":{"redis":{"status":"UP"},...},"groups":["liveness","readiness"]}

curl localhost:8087/actuator/health/liveness
# → {"status":"UP"}

curl localhost:8087/actuator/prometheus
# → jvm_memory_used_bytes{...}  system_cpu_usage{...}  http_server_requests_seconds_count{...}
```

**Health Probes en Terraform (`terraform/modules/microservice/main.tf:64-88`):**
```hcl
dynamic "liveness_probe" {
  content {
    http_get {
      path = var.probe_path       # /actuator/health/liveness
      port = var.container_port
    }
    initial_delay_seconds = 60
    period_seconds        = 15
    failure_threshold     = 3
  }
}
```

**3 Dashboards Grafana pre-configurados (`config/grafana/dashboards/`):**
| Dashboard | UID | Paneles |
|-----------|-----|---------|
| **Service Health** | `circleguard-services` | 5 paneles: servicios UP, HTTP rate, errores, latencia P95, CPU |
| **JVM Metrics** | `circleguard-jvm` | 4 paneles: heap memory, GC pauses, threads, class loading |
| **Business Metrics** | `circleguard-business` | 6 paneles: usuarios activos, QR, notifs, verificaciones, forms, Kafka |

**5 Alertas Prometheus (`config/prometheus/alert-rules.yml`):**
| Alerta | Condición | Severidad |
|--------|-----------|-----------|
| ServiceDown | `up == 0` por 1m | CRITICAL |
| HighErrorRate | 5xx > 5% por 2m | CRITICAL |
| HighResponseTime | P95 > 5s por 5m | WARNING |
| HighMemoryUsage | Heap > 85% por 5m | WARNING |
| HighCpuUsage | CPU > 80% por 5m | WARNING |

**Métricas de negocio (código `BusinessMetricsService.java`):**
```java
// 6 métricas registradas en MeterRegistry:
Counter.builder("circleguard_qr_codes_generated_total")...
Counter.builder("circleguard_notifications_sent_total")...
Counter.builder("circleguard_identity_verifications_total")...
Counter.builder("circleguard_form_submissions_total")...
Counter.builder("circleguard_kafka_messages_total")...
registry.gauge("circleguard_active_users_total", activeUsers, AtomicLong::doubleValue);

// Datos simulados cada 3-15 segundos para demo
@Scheduled(fixedRate = 3000) public void simulateQrGeneration() {
    qrCodesGenerated.increment(random.nextInt(3) + 1);
}
```

**Tracing distribuido (Jaeger):** Cada servicio envía trazas vía Micrometer Tracing (Brave + Zipkin):
```
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://jaeger:9411/api/v2/spans
```

**Para iniciar el stack de monitoreo:**
```bash
docker compose -f docker-compose.full.yml up -d prometheus grafana jaeger
# URLs: Grafana http://localhost:3001 (admin/admin)
#       Prometheus http://localhost:9090
#       Jaeger http://localhost:16686
```

---

## Slide 12: Seguridad (5%)

| Capa | Medida | Implementación |
|------|--------|---------------|
| **Código** | SCA (Software Composition Analysis) | OWASP Dependency-Check en Jenkins + `build.gradle.kts` |
| **Aplicación** | Autenticación | LDAP + JWT (Auth Service) |
| **Aplicación** | Anonimización | SHA-256 + UUID en Identity Service |
| **Infraestructura** | Secretos | Kubernetes Secrets via módulo Terraform `secrets/` |
| **Infraestructura** | RBAC | ServiceAccount + Role + RoleBinding por servicio |
| **Red** | TLS | Ingress + cert-manager + self-signed certs |
| **Pipeline** | DAST | OWASP ZAP escanea Gateway API |
| **Pipeline** | Container Security | Trivy escanea imágenes Docker |

**OWASP Dependency-Check en `build.gradle.kts`:**
```kotlin
// Plugin aplicado a todos los subproyectos
id("org.owasp.dependencycheck") version "9.2.0"
// Ejecutado en pipeline: ./gradlew dependencyCheckAnalyze --no-daemon || true
```

**Secretos en Kubernetes (`terraform/modules/secrets/main.tf`):**
```hcl
resource "kubernetes_secret" "this" {
  metadata { name = "circleguard-secrets" }
  data = var.secret_data  # sensitive, desde variables de Terraform Cloud
}
# Referenciado en deployments via env_from:
env_from {
  secret_ref { name = "circleguard-secrets" }
}
```

**RBAC (`terraform/modules/rbac/main.tf`):**
```hcl
resource "kubernetes_service_account" "this" {
  metadata { name = var.service_account_name }
}
resource "kubernetes_role" "this" {
  rule {
    api_groups = [""]
    resources  = ["pods", "services", "configmaps"]
    verbs      = ["get", "list", "watch"]
  }
}
resource "kubernetes_role_binding" "this" {
  subject {
    kind      = "ServiceAccount"
    name      = kubernetes_service_account.this.metadata[0].name
  }
  role_ref {
    kind      = "Role"
    name      = kubernetes_role.this.metadata[0].name
  }
}
```

**Scripts de seguridad:**
- `scripts/init-secrets.sh` — Template para crear secrets en K8s
- `scripts/gen-certs.sh` — Generación de certificados TLS auto-firmados

---

## Slide 13: Costos de Infraestructura

| Escenario | Costo/mes | Detalle |
|-----------|-----------|---------|
| **Desarrollo local** (Docker Desktop) | **$0** | Sin costo de nube, Docker Desktop es gratuito para uso personal |
| **Cloud completo** (EKS/GKE) | **~$240/mes** | 3 nodos t3.medium (~$90) + RDS (~$50) + ELK (~$60) + Prometheus/Grafana (~$40) |
| **Optimizado** | **~$135/mes** | Spot instances, auto-hospedado Prometheus/Grafana, BD en el mismo nodo |

**Costos evitados por usar Docker Desktop local:**
- Sin costo de instancias EC2/EKS: ~$240/mes ahorrados
- Sin costo de RDS: ~$50/mes ahorrados
- Sin costo de Elastic Cloud: ~$60/mes ahorrados
- **Total: ~$350/mes vs cloud completo**

---

## Slide 14: Lecciones Aprendidas

**Las 5 lecciones más importantes:**

1. **Terraform + K8s:** La curva de aprendizaje es alta (HCL, Kubernetes provider, estado remoto), pero el resultado — infraestructura reproducible en 3 ambientes — vale la pena.

2. **Pruebas con `@MockBean`:** Encontrar el tipo genérico correcto para `KafkaTemplate<String,Object>` vs `KafkaTemplate<String,String>` causó errores de compilación difíciles de depurar en los tests del notification-service. Se solucionó con `@MockBean(AuditLogService.class)` en 5 clases de test.

3. **Trivy timeout:** La descarga inicial de la base de datos de vulnerabilidades (878 MB Java, 1.2 GB total) excede el timeout por defecto de 5 minutos. Solución: `--timeout 10m` en el comando Trivy.

4. **DNS en Docker BuildKit:** Los contenedores de build de Docker no resolvían `registry-1.docker.io`. Solución: pre-pull de imágenes base (`gradle:8.7-jdk21`, `eclipse-temurin:21-jre-alpine`) y agregar `"dns": ["8.8.8.8", "1.1.1.1"]` en `daemon.json`.

5. **Monitoreo desde el inicio:** Agregar Actuator, métricas Micrometer y health probes desde el día 1 del desarrollo simplifica enormemente la depuración en ambientes de stage/producción.

**Problema técnico específico — Kotlin DSL de OWASP Dependency-Check:**
- Error: `Unresolved reference: dependencyCheck` en `build.gradle.kts`
- Causa: El plugin `org.owasp.dependencycheck` versión 9.2.0 no registra la extensión `dependencyCheck` en Kotlin DSL cuando se aplica via `apply(plugin = ...)` en subproyectos
- Solución: Usar `tasks.named("dependencyCheckAnalyze")` en vez de `dependencyCheck { }`
- Referencia: commit `8670b7e`

---

## Slide 15: Demo en Vivo

**Escena 1 — Jenkins Pipeline (3 min):**
1. Abrir http://localhost:8080
2. Mostrar los 3 jobs: `circle-guard-dev`, `circle-guard-stage`, `circle-guard-master`
3. Abrir último build de DEV → mostrar stages verdes
4. Señalar: Build → Test → Coverage → Docker → Dependency Check → SonarQube → Trivy → ZAP

**Escena 2 — SonarQube (1 min):**
1. Abrir http://localhost:9000
2. Mostrar Quality Gate: **Passed**, cobertura ≥70%, 0 bugs, 0 vulnerabilidades

**Escena 3 — Health Checks (1 min):**
```bash
curl localhost:8087/actuator/health
curl localhost:8087/actuator/health/liveness
curl localhost:8087/actuator/prometheus | head -20
```

**Escena 4 — Grafana Dashboards (3 min):**
1. Abrir http://localhost:3001 → admin/admin
2. Dashboard → CircleGuard → **Service Health**: servicios UP, HTTP rate, CPU
3. Dashboard → CircleGuard → **Business Metrics**: QR, notificaciones, usuarios activos (datos simulados)
4. Dashboard → CircleGuard → **JVM Metrics**: heap, GC, threads

**Escena 5 — Jaeger Tracing (1 min):**
1. Abrir http://localhost:16686
2. Seleccionar servicio: `gateway-service`
3. Buscar trazas recientes → mostrar span details

**Escena 6 — Terraform (1 min):**
```bash
kubectl get pods -n circleguard
kubectl get namespaces | grep circleguard
```

---

## Slide 16: Resultados vs Requisitos

| # | Requisito | % | Estado | Evidencia |
|---|-----------|---|--------|-----------|
| 1 | Metodología Ágil | 5% | ✅ | `docsagil/`, Kanban con 16 HUs |
| 2 | Infraestructura como Código | 15% | ✅ | `terraform/`, 5 módulos, 3 ambientes |
| 3 | Patrones de Diseño | 10% | ✅ | `docspatrones/`, 9 patrones, 4 nuevos |
| 4 | CI/CD Avanzado | 15% | ✅ | 3 Jenkinsfiles, SonarQube, Trivy, versionado |
| 5 | Pruebas Completas | 15% | ✅ | JUnit 5, JaCoCo 70%, ZAP, Dependency-Check |
| 6 | Change Management | 5% | ✅ | CRQ, CAB, release notes, rollback |
| 7 | Observabilidad | 10% | ✅ | Prometheus, Grafana, Jaeger, ELK |
| 8 | Seguridad | 5% | ✅ | SCA, Secrets, RBAC, TLS |
| 9 | Documentación | 10% | ✅ | `docs/`, informe final, presentación |
| | **Total** | **90%** | ✅ | |

---

## Slide 17: Conclusiones

1. **Circle Guard cumple con los 9 requisitos** del proyecto — desde metodología ágil hasta documentación final.

2. **Arquitectura de microservicios** con 8 servicios independientes permite escalar, desplegar y mantener cada componente de forma individual.

3. **Privacidad desde el diseño:** La anonimización irreversible de identidades (SHA-256 + UUID) garantiza que ni siquiera el operador del sistema puede re-identificar a los usuarios.

4. **Automatización total:** 3 pipelines Jenkins con build, test, cobertura, seguridad, despliegue, release notes, tagging y GitHub Release — todo automático con aprobación manual solo para producción.

5. **Observabilidad completa:** Prometheus + Grafana + Jaeger + ELK stack proporcionan métricas, tracing y logs centralizados para los 8 servicios.

6. **El sistema está listo para producción real** en cualquier universidad que necesite control de acceso sanitario con privacidad garantizada.

---

## Slide 18: Preguntas

**Contacto:**
- Email: juliianavalenciia21@gmail.com
- Repositorio: https://github.com/JuliianaV2106/circle-guard-public
- Kanban: https://github.com/users/JuliianaV2106/projects/2

**Enlaces rápidos para la demo:**
- Jenkins: http://localhost:8080 (admin/1107842545)
- SonarQube: http://localhost:9000 (admin/admin)
- Grafana: http://localhost:3001 (admin/admin)
- Jaeger: http://localhost:16686
- Prometheus: http://localhost:9090

---

## Notas para el Presentador

| Slide | Tiempo | Acción | Comando/URL |
|-------|--------|--------|-------------|
| 1-2 | 2 min | Hablar, no mostrar nada técnico | — |
| 3 | 1 min | Mostrar el diagrama de flujo | Señalar los 8 servicios |
| 4 | 2 min | Mostrar tabla de servicios | Abrir `docker ps` en terminal |
| 5 | 1 min | Mostrar GitHub Projects | https://github.com/users/JuliianaV2106/projects/2 |
| 6 | 2 min | Mostrar Terraform + K8s | `kubectl get pods -n circleguard` + `code terraform/environments/dev/main.tf` |
| 7 | 2 min | Mostrar código + docs | `code IdentityClient.java` + abrir `docspatrones/index.md` |
| 8 | 3 min | **DEMO**: Jenkins | http://localhost:8080 → mostrar último build verde |
| 9 | 2 min | **DEMO**: SonarQube + tests | http://localhost:9000 + mostrar `locustfile.py` |
| 10 | 1 min | Mostrar docs | `code docschange-management/index.md` |
| 11 | 3 min | **DEMO**: Grafana + Jaeger | http://localhost:3001 → 3 dashboards + http://localhost:16686 |
| 12 | 1 min | Mostrar secrets + RBAC | `code terraform/modules/secrets/main.tf` |
| 13 | 1 min | Mostrar tabla de costos | Señalar $0 local vs ~$240 cloud |
| 14 | 1 min | Mencionar lecciones | Las 5 más importantes |
| 15 | 3 min | **DEMO**: Live | Comandos curl + Jenkins + Grafana |
| 16 | 1 min | Mostrar tabla de resultados | Señalar 90% completado |
| 17-18 | 2 min | Cierre + preguntas | — |

**Preparación previa a la presentación:**
```bash
# 1. Verificar que Jenkins esté corriendo
docker ps | grep jenkins

# 2. Verificar pipelines verdes
# Abrir http://localhost:8080 → cada job debe mostrar build exitoso

# 3. Iniciar stack de monitoreo
docker compose -f docker-compose.full.yml up -d

# 4. Verificar gateway-service activo
curl -s localhost:8087/actuator/health

# 5. Verificar métricas en Prometheus
curl -s http://localhost:9090/api/v1/query?query=up

# 6. Pre-abrir en el navegador:
#    - http://localhost:8080 (Jenkins)
#    - http://localhost:9000 (SonarQube)
#    - http://localhost:3001 (Grafana)
#    - http://localhost:16686 (Jaeger)
#    - http://localhost:9090 (Prometheus)

# 7. Abrir terminal con:
cd ~/Desktop/8\ Semestre/Ingesoft\ V/proyecto\ final/circle-guard-public
```
