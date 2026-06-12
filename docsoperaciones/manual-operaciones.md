# Manual de Operaciones — Circle Guard

## 1. Descripción General

Circle Guard es un sistema de control de acceso sanitario universitario compuesto por 8 microservicios. Este manual cubre las operaciones diarias, despliegue, monitoreo y resolución de problemas.

## 2. Arquitectura

```
Gateway (8087) → Auth (8180) → LDAP
              → Identity (8083) → PostgreSQL
              → Form (8086) → PostgreSQL → Kafka
              → Notification (8082) → Kafka → SMTP/Twilio
              → Dashboard (8084) → PostgreSQL
              → File (8085)
              → Promotion (8088) → PostgreSQL + Neo4j + Redis → Kafka
```

## 3. Requisitos del Sistema

| Requisito | Versión Mínima |
|-----------|---------------|
| Docker Desktop | 4.27+ |
| Kubernetes (Docker Desktop) | 1.28+ |
| JDK | 21 (Temurin) |
| Gradle | 8.7+ |
| Git | 2.40+ |
| RAM | 16 GiB (mínimo), 32 GiB (recomendado) |
| Disco | 50 GiB libres |

## 4. Despliegue

### 4.1 Local (Docker Compose)

```bash
# Iniciar infraestructura + servicios
docker compose -f docker-compose.full.yml up -d

# Ver estado
docker compose -f docker-compose.full.yml ps

# Ver logs de un servicio
docker compose -f docker-compose.full.yml logs -f gateway-service

# Detener todo
docker compose -f docker-compose.full.yml down
```

### 4.2 Local (Kubernetes + Terraform)

```bash
# 1. Construir imágenes
docker build -f Dockerfile.auth-service -t circleguard/auth-service:latest .
docker build -f Dockerfile.gateway-service -t circleguard/gateway-service:latest .
# ... repetir para cada servicio

# 2. Inicializar secretos
./scripts/init-secrets.sh circleguard

# 3. Desplegar con Terraform
cd terraform/environments/dev
terraform init
terraform apply -auto-approve -var-file=dev.tfvars
```

### 4.3 Pipeline CI/CD (Jenkins)

Los pipelines se ejecutan automáticamente al hacer push a:
- `develop` → Jenkinsfile (build + test + scan)
- `master` → Jenkinsfile.master (release completo)

#### Pipeline Master (Release)

```
Checkout → Semantic Versioning → Build → Tests → SonarQube
→ Docker Build → Trivy → ZAP → Deploy Stage → System Tests
→ Approval → Deploy Master → Release Notes → Tag → GitHub Release
```

## 5. Monitoreo

### 5.1 Health Checks

Cada servicio expone endpoints de health check:

```bash
# Verificar estado de todos los servicios
curl http://localhost:8087/actuator/health        # Gateway
curl http://localhost:8180/actuator/health        # Auth
curl http://localhost:8083/actuator/health        # Identity
curl http://localhost:8086/actuator/health        # Form
curl http://localhost:8082/actuator/health        # Notification
curl http://localhost:8084/actuator/health        # Dashboard
curl http://localhost:8085/actuator/health        # File
curl http://localhost:8088/actuator/health        # Promotion
```

### 5.2 Dashboards

| Herramienta | URL | Credenciales |
|-------------|-----|-------------|
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3001 | admin / admin |
| Jaeger | http://localhost:16686 | — |
| Kibana | http://localhost:5601 | — |
| Jenkins | http://localhost:8080 | admin / 1107842545 |
| SonarQube | http://localhost:9000 | admin / admin |

### 5.3 Alertas Críticas

| Alerta | Acción |
|--------|--------|
| Service Down | Revisar logs del servicio, reiniciar pod |
| Error Rate > 5% | Revisar dependencias (BD, Kafka, LDAP) |
| Memory > 85% | Aumentar recursos del pod |
| CPU > 80% | Escalar horizontalmente |

## 6. Logs

### 6.1 Logs de Contenedores (Docker)

```bash
docker logs -f circleguard-auth-service
docker logs -f circleguard-gateway-service
```

### 6.2 Logs de Kubernetes

```bash
kubectl logs -n circleguard deployment/auth-service
kubectl logs -n circleguard-master deployment/gateway-service
```

### 6.3 ELK Stack

Los logs se centralizan en Elasticsearch y se visualizan en Kibana:
1. Abrir http://localhost:5601
2. Crear index pattern: `circleguard-logs-*`
3. Explorar logs por servicio, nivel, o timestamp

## 7. Backup y Restauración

### 7.1 Base de Datos

```bash
# Backup de PostgreSQL
docker exec circleguard-postgres pg_dump -U admin circleguard_auth > backup-auth.sql
docker exec circleguard-postgres pg_dump -U admin circleguard_identity > backup-identity.sql

# Restore
cat backup-auth.sql | docker exec -i circleguard-postgres psql -U admin -d circleguard_auth
```

### 7.2 Terraform State

El estado de Terraform se almacena en Terraform Cloud (workspaces organizados por ambiente).

### 7.3 Jenkins

Los jobs de Jenkins tienen sus configs respaldadas en `JENKINS_HOME`. Respaldar:
```bash
docker cp jenkins:/var/jenkins_home/jobs ./jenkins-backup/
```

## 8. Rollback

### 8.1 Rollback de Servicio

```bash
# Kubernetes
kubectl rollout undo deployment/gateway-service -n circleguard-master

# A versión específica
kubectl set image deployment/gateway-service \
  gateway-service=circleguard/gateway-service:v1.0.0 \
  -n circleguard-master
```

### 8.2 Rollback de Terraform

```bash
# Re-aplicar versión anterior
cd terraform/environments/master
git checkout <commit-anterior> -- .
terraform apply -auto-approve
```

### 8.3 Rollback de Base de Datos

```bash
# Restaurar backup
cat backup-auth-2026-06-10.sql | docker exec -i circleguard-postgres psql -U admin -d circleguard_auth

# Migraciones Flyway (reparar)
docker exec circleguard-auth-service flyway repair
```

## 9. Troubleshooting Común

| Problema | Causa Posible | Solución |
|----------|--------------|----------|
| Gateway no responde | Redis no disponible | `docker compose restart redis` |
| Auth falla al loguear | LDAP no disponible | Verificar `docker compose ps openldap` |
| Kafka messages no llegan | Consumer group offset | Reset offset: `kafka-consumer-groups --reset-offsets` |
| Pod en CrashLoopBackOff | Falta de memoria o error de config | `kubectl describe pod <pod>` |
| Terraform apply falla | State lock | `terraform force-unlock <lock-id>` |

## 10. Mantenimiento Programado

| Tarea | Frecuencia | Comando |
|-------|-----------|---------|
| Actualizar dependencias | Semanal | `./gradlew dependencyCheckAnalyze` |
| Rotar secretos | Mensual | `./scripts/init-secrets.sh` |
| Backup BD | Diario | Script `pg_dump` |
| Limpiar imágenes Docker viejas | Semanal | `docker image prune -a` |
| Revisar logs de errores | Diario | Kibana dashboard |
| Actualizar pipelines | Por release | Editar Jenkinsfiles |

## 11. Contactos y Responsabilidades

| Rol | Responsable | Contacto |
|-----|-------------|----------|
| DevOps / Admin | Juliana Filigrana | juliianavalenciia21@gmail.com |
| Developer | Juliana Filigrana | Repositorio GitHub |
| Product Owner | Juliana Filigrana | GitHub Projects |
