# Costos de Infraestructura — Circle Guard

## Escenario: Ejecución Local (Docker Desktop)

Sin costos de nube. Todos los servicios corren en máquina local.

| Recurso | Costo |
|---------|-------|
| Docker Desktop (licencia personal) | Gratuito |
| IDE (IntelliJ Community / VS Code) | Gratuito |
| JDK 21 (Temurin) | Gratuito |
| PostgreSQL 16, Neo4j, Kafka, Redis, LDAP | Open source — Gratuito |
| **Total mensual** | **$0 USD** |

## Escenario: Producción en Kubernetes (Nube)

Estimación para despliegue en cloud (AWS EKS o GCP GKE) con 3 ambientes.

### Compute (EKS / GKE)

| Entorno | Servicios | Replicas | CPU/Request | Memory/Request | Instancias |
|---------|-----------|----------|-------------|----------------|------------|
| DEV | 6 servicios | 1 | 0.5 CPU | 512 MiB | 2 x t3.small |
| STAGE | 2 servicios | 1 | 0.5 CPU | 512 MiB | 1 x t3.small |
| MASTER | 2 servicios | 2 | 1.0 CPU | 1 GiB | 2 x t3.medium |

| Entorno | Tipo instancia | Costo/hora | Horas/mes | Costo/mes |
|---------|---------------|------------|-----------|-----------|
| DEV | 2 x t3.small (0.0209 USD/h) | $0.0418 | 730 | $30.51 |
| STAGE | 1 x t3.small (0.0209 USD/h) | $0.0209 | 730 | $15.26 |
| MASTER | 2 x t3.medium (0.0416 USD/h) | $0.0832 | 730 | $60.74 |
| **Total compute** | | | | **$106.51/mes** |

### Servicios Administrados

| Servicio | Propósito | Costo estimado/mes |
|----------|-----------|-------------------|
| ECR / Docker Registry | Almacenamiento de imágenes | $5.00 |
| RDS PostgreSQL | Base de datos relacional | $25.00 (db.t3.small) |
| MemoryDB Redis | Caché | $20.00 |
| MSK Kafka | Message broker | $30.00 (1 broker) |
| Load Balancer | Balanceo de tráfico | $20.00 |
| **Total servicios** | | **$100.00/mes** |

### Monitoreo

| Servicio | Propósito | Costo/mes |
|----------|-----------|-----------|
| Prometheus (auto-hospedado) | Métricas | Incluido en compute |
| Grafana (auto-hospedado) | Dashboards | Incluido en compute |
| Jaeger (auto-hospedado) | Tracing | Incluido en compute |
| Elasticsearch + Kibana | Logs | $30.00 (1 x t3.small) |
| **Total monitoreo** | | **$30.00/mes** |

### Almacenamiento

| Tipo | Tamaño estimado | Costo/mes |
|------|----------------|-----------|
| EBS gp3 (volúmenes K8s) | 50 GiB | $4.00 |
| S3 (backups, certificados) | 10 GiB | $0.23 |
| **Total almacenamiento** | | **$4.23/mes** |

### Resumen de Costos Mensuales

| Categoría | Costo/mes |
|-----------|-----------|
| Compute (EKS/GKE) | $106.51 |
| Servicios administrados | $100.00 |
| Monitoreo | $30.00 |
| Almacenamiento | $4.23 |
| **Total** | **$240.74/mes** |
| **Total anual** | **$2,888.88/año** |

## Escenario: Desarrollo Local + Producción en Nube (Recomendado)

| Componente | Costo/mes |
|------------|-----------|
| Desarrollo local (Docker Desktop) | $0 |
| Producción (K8s cloud) | $240.74 |
| **Total** | **$240.74/mes** |

## Optimizaciones Posibles

1. **Uso de spot instances**: Reducción del 60-70% en compute para DEV/STAGE
2. **Auto-scaling**: Reducir a 0 réplicas fuera del horario académico (7PM-7AM)
3. **Tipo de instancia ARM (Graviton)**: 20-30% más barato que x86
4. **Kafka Serverless**: Sin costo por broker inactivo
5. **ELK serverless**: Usar Elastic Cloud Serverless para logs

## Costos Evitados

| Servicio | Alternativa gratuita | Ahorro/mes |
|----------|---------------------|------------|
| RDS PostgreSQL | PostgreSQL auto-hospedado en K8s | $25.00 |
| MemoryDB Redis | Redis auto-hospedado en K8s | $20.00 |
| MSK Kafka | Kafka auto-hospedado en K8s | $30.00 |
| ELK cloud | Elasticsearch auto-hospedado en K8s | $30.00 |
| **Total evitado** | | **$105.00/mes** |

Con todas las optimizaciones, el costo mínimo para producción sería de ~**$135/mes**.
