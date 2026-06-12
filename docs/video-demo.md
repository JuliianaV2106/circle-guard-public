# Video Demostrativo — Circle Guard

## Información General

| Campo | Descripción |
|-------|-------------|
| **Duración estimada** | 8-10 minutos |
| **Formato** | Grabación de pantalla + narración |
| **Herramientas** | OBS Studio (grabación), DaVinci Resolve (edición) |
| **Idioma** | Español |

---

## Escena 1: Introducción (0:00 — 1:00)

**Visual**: Pantalla con logo de Circle Guard + nombres del proyecto y autora

**Narración**:
> "Circle Guard es un sistema de control de acceso sanitario universitario. Su objetivo es identificar círculos de contacto entre estudiantes y aplicar cercos sanitarios rápidos, preservando la privacidad mediante anonimización de identidades. Este proyecto fue desarrollado como parte del curso de Ingeniería de Software V."

---

## Escena 2: Arquitectura General (1:00 — 2:00)

**Visual**: Diagrama de arquitectura (8 microservicios + infraestructura)

**Narración**:
> "El sistema está compuesto por 8 microservicios en Spring Boot 3.2, orquestados con Gradle. La infraestructura corre en Kubernetes sobre Docker Desktop. Usamos PostgreSQL como base de datos relacional, Neo4j para el grafo de contactos, Kafka como bus de eventos, Redis como caché, y OpenLDAP para autenticación universitaria."

**Acciones**: Mostrar `docs/arquitectura/overview.md` o el diagrama visual

---

## Escena 3: Infraestructura como Código (2:00 — 3:00)

**Visual**: Terminal + Terraform + K8s

**Narración**:
> "La infraestructura está definida como código usando Terraform con tres ambientes: dev, stage y master. Cada ambiente tiene su propio namespace en Kubernetes. Los módulos Terraform permiten desplegar microservicios con health probes, configmaps y service accounts de forma declarativa."

**Acciones**:
1. `cd terraform/environments/dev`
2. `terraform show` (mostrar recursos)
3. `kubectl get pods -n circleguard`

---

## Escena 4: Pipeline CI/CD (3:00 — 4:30)

**Visual**: Jenkins UI + pipeline corriendo

**Narración**:
> "La integración continua se maneja con Jenkins en tres pipelines. Al hacer push a develop, se ejecuta el pipeline completo de build, pruebas, análisis estático con SonarQube, escaneo de dependencias con OWASP Dependency-Check, vulnerabilidades en imágenes con Trivy, y pruebas de penetración con OWASP ZAP. El pipeline master incluye además versionado semántico automático, despliegue a stage con aprobación manual, despliegue a producción y generación de release notes."

**Acciones**:
1. Mostrar Jenkins en http://localhost:8080
2. Navegar a un pipeline build exitoso
3. Mostrar los stages: Build → Test → Dependency Check → SonarQube → Docker Build → Trivy → ZAP

---

## Escena 5: Monitoreo y Observabilidad (4:30 — 6:00)

**Visual**: Grafana + Prometheus + Jaeger + Kibana

**Narración**:
> "La observabilidad se implementó con Prometheus para métricas, Grafana para dashboards, Jaeger para tracing distribuido y ELK para logs. Cada servicio expone métricas vía Micrometer en /actuator/prometheus. Tenemos dashboards pre-configurados para health de servicios, métricas JVM y métricas de negocio."

**Acciones**:
1. Mostrar Grafana en http://localhost:3001 (dashboards)
2. Mostrar Prometheus en http://localhost:9090 (targets UP)
3. Mostrar Jaeger en http://localhost:16686 (traces)
4. Mostrar Kibana en http://localhost:5601 (logs)

---

## Escena 6: Seguridad (6:00 — 7:00)

**Visual**: Dependency-Check report + Trivy output + secrets management

**Narración**:
> "En seguridad implementamos cuatro capas: escaneo continuo de vulnerabilidades con OWASP Dependency-Check, gestión segura de secretos usando Kubernetes Secrets en lugar de valores hardcodeados, RBAC con ServiceAccounts individuales por microservicio, y TLS para el gateway mediante Ingress con certificados autofirmados."

**Acciones**:
1. Mostrar reporte de Dependency-Check
2. Mostrar `kubectl get secrets -n circleguard`
3. Mostrar `kubectl get serviceaccounts -n circleguard`

---

## Escena 7: Demostración en Vivo (7:00 — 8:30)

**Visual**: API calls con curl + respuesta del sistema

**Narración**:
> "Vamos a hacer una demostración en vivo del sistema. Primero, autenticamos un usuario contra el auth-service, luego validamos un QR en el gateway, y finalmente consultamos el estado de salud."

**Acciones**:
```bash
# 1. Login
curl -X POST http://localhost:8180/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "student", "password": "password"}'

# 2. Validar QR (Gateway)
curl -X POST http://localhost:8087/api/v1/gate/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "<jwt-token>"}'

# 3. Health check
curl http://localhost:8087/actuator/health
```

---

## Escena 8: Costos y Cierre (8:30 — 10:00)

**Visual**: Tabla de costos + Kanban final

**Narración**:
> "Los costos de infraestructura en producción se estiman en aproximadamente $240 USD mensuales, que pueden reducirse a $135 usando instancias spot y servicios auto-hospedados. El desarrollo local no tiene costo gracias a Docker Desktop y herramientas open source."

> "El proyecto sigue una metodología ágil con Kanban en GitHub Projects, GitFlow para branching, y 16 historias de usuario organizadas en 3 sprints. El repositorio está organizado con conventional commits y versionado semántico automático."

> "En conclusión, Circle Guard demuestra cómo integrar microservicios, infraestructura como código, CI/CD, observabilidad y seguridad en un sistema universitario funcional."

**Acciones**: Mostrar Kanban board final con todas las historias completadas

---

## Recursos para la Grabación

| Elemento | Recomendación |
|----------|---------------|
| Resolución | 1920x1080 (1080p) |
| FPS | 30 |
| Audio | Micrófono externo, narración clara |
| Música | Libre de derechos (opcional, volumen bajo) |
| Fuente | Monospace para terminal, Sans-serif para slides |
