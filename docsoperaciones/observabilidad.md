# Observabilidad y Monitoreo — Circle Guard

## Arquitectura de Observabilidad

```
                         ┌──────────────────────────────────────┐
                         │             Grafana                  │
                         │  Dashboards + Alerting               │
                         └──────┬──────────────┬───────────────┘
                                │              │
                    ┌───────────┘              └───────────┐
                    ▼                                        ▼
          ┌─────────────────┐                    ┌──────────────────┐
          │   Prometheus    │                    │   Elasticsearch  │
          │  Metrics Store  │                    │   Logs Store     │
          └──────┬──────────┘                    └────────┬─────────┘
                 │                                        │
                 ▼                                        ▼
          ┌─────────────────┐                    ┌──────────────────┐
          │  /actuator/     │                    │    Logstash      │
          │  prometheus     │                    │  Log Processor   │
          └─────────────────┘                    └──────────────────┘
                 │                                        │
                 ▼                                        ▼
          ┌─────────────────┐                    ┌──────────────────┐
          │ 8 Microservices │                    │   Docker/K8s     │
          │ (Micrometer)    │                    │   Log Output     │
          └─────────────────┘                    └──────────────────┘

                    ┌──────────────────┐
                    │      Jaeger      │
                    │ Distributed      │
                    │ Tracing (Zipkin) │
                    └──────────────────┘
                           ▲
                           │
                    ┌──────┴──────┐
                    │ Micrometer  │
                    │ Tracing     │
                    │ (Brave)     │
                    └─────────────┘
```

## Componentes

| Componente | Propósito | Puerto | Imagen |
|-----------|-----------|--------|--------|
| **Prometheus** | Almacenamiento y consulta de métricas | 9090 | prom/prometheus:v2.53.0 |
| **Grafana** | Dashboards y alertas visuales | 3001 | grafana/grafana:11.0.0 |
| **Jaeger** | Tracing distribuido (Zipkin API) | 16686, 9411 | jaegertracing/all-in-one:1.58 |
| **Elasticsearch** | Almacenamiento de logs | 9200 | elastic/elasticsearch:8.11.0 |
| **Logstash** | Procesamiento de logs | 5000, 5044 | logstash:8.11.0 |
| **Kibana** | Visualización de logs | 5601 | kibana:8.11.0 |

## Configuración en Microservicios

### Dependencias (build.gradle.kts)

Cada servicio incluye automáticamente:
- `spring-boot-starter-actuator` — Health checks, métricas, info
- `micrometer-registry-prometheus` — Métricas en formato Prometheus
- `micrometer-tracing-bridge-brave` — Tracing distribuido
- `zipkin-reporter-brave` — Envío de trazas a Zipkin/Jaeger

### application.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
  tracing:
    sampling:
      probability: 1.0
```

### Endpoints expuestos por servicio

| Endpoint | Propósito |
|----------|-----------|
| `/actuator/health` | Health check general |
| `/actuator/health/liveness` | Liveness probe (K8s) |
| `/actuator/health/readiness` | Readiness probe (K8s) |
| `/actuator/metrics` | Métricas JVM y de aplicación |
| `/actuator/prometheus` | Métricas formato Prometheus |
| `/actuator/info` | Información del servicio |

## Health Checks y Probes

Todos los servicios tienen liveness y readiness probes configuradas:

| Parámetro | Liveness | Readiness |
|-----------|----------|-----------|
| Path | `/actuator/health/liveness` | `/actuator/health/readiness` |
| Initial delay | 60s | 30s |
| Period | 15s | 10s |
| Failure threshold | 3 | 3 |

### Terraform

```hcl
module "auth_service" {
  source                 = "../../modules/microservice"
  enable_liveness_probe  = true
  enable_readiness_probe = true
  probe_path             = "/actuator/health"
  liveness_initial_delay = 90
  readiness_initial_delay = 45
}
```

## Prometheus Alertas

Archivo: `config/prometheus/alert-rules.yml`

| Alerta | Condición | Severidad |
|--------|-----------|-----------|
| ServiceDown | `up == 0` por 1m | CRITICAL |
| HighErrorRate | 5xx > 5% por 2m | CRITICAL |
| HighResponseTime | P95 > 5s por 5m | WARNING |
| HighMemoryUsage | Heap > 85% por 5m | WARNING |
| HighCpuUsage | CPU > 80% por 5m | WARNING |

## Grafana Dashboards

Tres dashboards pre-configurados:

| Dashboard | UID | Descripción |
|-----------|-----|-------------|
| **Service Health** | circleguard-services | Estado de servicios, tasa de requests, errores HTTP, latencia P95, CPU |
| **JVM Metrics** | circleguard-jvm | Memoria heap/non-heap, GC pauses, threads, clases cargadas |
| **Business Metrics** | circleguard-business | Usuarios activos, QR generados, notificaciones, verificaciones, formularios, Kafka |

Los dashboards se cargan automáticamente vía provisioning (`config/grafana/dashboards/`).

## Tracing Distribuido (Jaeger)

Cada servicio envía trazas a Jaeger via Zipkin API:

```
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://jaeger:9411/api/v2/spans
```

Acceso a Jaeger UI: http://localhost:16686

## ELK Stack (Logs)

Logstash recibe logs via:
- **Beats** (puerto 5044) — logs desde contenedores
- **TCP** (puerto 5000) — logs en formato JSON

Los logs se indexan en Elasticsearch como `circleguard-logs-YYYY.MM.dd`.

Kibana UI: http://localhost:5601

## Docker Compose

Para iniciar todo el stack de monitoreo:

```bash
docker compose -f docker-compose.full.yml up -d
```

Esto inicia todos los microservicios + monitoreo.

## Ejecución Local (sin Docker Compose)

```bash
# Iniciar infraestructura base
docker compose up -d postgres neo4j kafka redis

# Iniciar monitoreo
docker compose up -d prometheus grafana jaeger elasticsearch logstash kibana

# Iniciar microservicios (cada uno en su terminal)
./gradlew :services:circleguard-gateway-service:bootRun
```

## URLs de Acceso

| Herramienta | URL |
|-------------|-----|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 (admin/admin) |
| Jaeger | http://localhost:16686 |
| Kibana | http://localhost:5601 |
| Elasticsearch | http://localhost:9200 |

---

*Documento controlado — Versión 1.0 — Última actualización: 2026-06-11*
