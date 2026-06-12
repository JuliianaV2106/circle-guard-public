# Presentación del Proyecto — Circle Guard

**Duración estimada:** 15-20 minutos  
**Formato:** PowerPoint / Google Slides / Canva  
**Idioma:** Español

---

## Slide 1: Portada

- Logo de Circle Guard
- Título: "Circle Guard — Control de Acceso Sanitario Universitario"
- Subtítulo: "Ingeniería de Software V — Semestre 8"
- Autora: Juliana Filigrana Valencia
- Fecha: Junio 2026

---

## Slide 2: Problema

- Brotes de enfermedades en campus universitarios
- Dificultad para identificar contactos rápidamente
- Riesgo de exposición de datos personales (privacidad)
- Falta de automatización en cercos sanitarios

**Pregunta clave:** ¿Cómo contener brotes en < 60 segundos sin exponer identidades?

---

## Slide 3: Solución — Circle Guard

**Diagrama simplificado:**
```
Usuario → QR → Gateway → Auth (LDAP + JWT)
                      → Graph (Neo4j) → Promoción de estado
                      → Notificaciones (Email/SMS/Push)
                      → Dashboard (Anonimizado)
```

**3 pilares:**
1. **Privacidad como código** — Anonimización de identidades
2. **Contención recursiva** — Promoción de estado en milisegundos
3. **Integración universitaria** — LDAP, horarios, WiFi

---

## Slide 4: Arquitectura Técnica

**8 microservicios + infraestructura:**

| Servicio | Puerto | Base de datos | Rol |
|----------|--------|---------------|-----|
| Auth Service | 8180 | PostgreSQL + LDAP | Autenticación dual |
| Gateway Service | 8087 | Redis | Validación QR |
| Identity Service | 8083 | PostgreSQL | Vault de identidades |
| Form Service | 8086 | PostgreSQL + Kafka | Encuestas de salud |
| Notification Service | 8082 | Kafka | Notificaciones multi-canal |
| Dashboard Service | 8084 | PostgreSQL | Dashboard anonimizado |
| File Service | 8085 | — | Subida de documentos |
| Promotion Service | 8088 | PostgreSQL + Neo4j + Redis | Motor de promoción |

---

## Slide 5: Metodología Ágil (5%)

- **Framework:** Kanban + GitFlow
- **Herramienta:** GitHub Projects
- **Sprints:** 3 sprints (32 + 47 pts cerrados, 3er sprint estimado)
- **16 historias de usuario** organizadas en 5 épicas
- **Documentación:** `docsagil/metodologia.md`

---

## Slide 6: Infraestructura como Código (5%)

- **Herramienta:** Terraform 1.6+
- **3 ambientes:** DEV, STAGE, MASTER
- **Módulos:** namespace, configmap, microservice, secrets, rbac
- **Estado remoto:** Terraform Cloud
- **Documentación:** `docsinfraestructura/index.md`

---

## Slide 7: Patrones de Diseño (5%)

| Patrón | Implementación | Servicio |
|--------|---------------|----------|
| Circuit Breaker | Resilience4j | Auth Service |
| Retry | Resilience4j | Auth Service |
| Time Limiter | Resilience4j | Auth Service |
| External Configuration | application.yml + K8s ConfigMap | Todos |
| Attribute Converter | JPA @Convert | Identity Service |

**Documentación:** `docspatrones/`

---

## Slide 8: CI/CD Avanzado (5%)

**3 pipelines Jenkins:**

| Pipeline | Evento | Stages Clave |
|----------|--------|-------------|
| DEV | Push a develop | Build → Test → Sonar → Trivy → ZAP |
| STAGE | Push a master (pre-release) | Terraform apply → Smoke tests → ZAP |
| MASTER | Release | Versionado semántico → Build → Tests → Sonar → Docker Build → Security Scan → ZAP → Deploy Stage → System Tests → Approval → Deploy Master → Release Notes → Tag → GitHub Release |

**Documentación:** `docsrelease-notes/process.md`

---

## Slide 9: Pruebas Completas (5%)

| Tipo | Herramienta | Cobertura |
|------|-------------|-----------|
| Unitarias | JUnit 5 + Mockito | 70%+ (verificado con JaCoCo) |
| Integración | Testcontainers | Servicios con BD/Kafka |
| SAST | SonarQube | Análisis estático |
| SCA | OWASP Dependency-Check | Dependencias Gradle |
| Container | Trivy | HIGH/CRITICAL |
| DAST | OWASP ZAP | Gateway API |

**Documentación:** `docspruebas/`

---

## Slide 10: Change Management (5%)

- **Proceso formal:** CRQ, CAB, tipos de cambio
- **Release Notes automáticas:** Script `scripts/generate-release-notes.sh`
- **Plan de rollback:** `kubectl rollout undo`
- **Versionado semántico:** Auto-incremento PATCH
- **GitHub Releases:** `gh release create`

**Documentación:** `docschange-management/`

---

## Slide 11: Observabilidad (10%)

**Stack completo:**

| Componente | Propósito | Puerto |
|-----------|-----------|--------|
| Prometheus | Métricas | 9090 |
| Grafana | Dashboards | 3001 |
| Jaeger | Tracing distribuido | 16686 |
| Elasticsearch | Almacenamiento de logs | 9200 |
| Logstash | Procesamiento de logs | 5044 |
| Kibana | Visualización de logs | 5601 |

**3 dashboards pre-configurados:** Service Health, JVM Metrics, Business Metrics

**Documentación:** `docsoperaciones/observabilidad.md`

---

## Slide 12: Seguridad (5%)

| Medida | Implementación |
|--------|---------------|
| SCA | OWASP Dependency-Check en pipelines |
| Secretos | Kubernetes Secrets + Terraform module |
| RBAC | ServiceAccount + Role por servicio |
| TLS | Ingress + cert-manager + self-signed certs |

**Documentación:** `docsseguridad/index.md`

---

## Slide 13: Costos de Infraestructura

| Escenario | Costo/mes |
|-----------|-----------|
| Desarrollo local (Docker Desktop) | $0 |
| Producción en nube (EKS/GKE) | ~$240/mes |
| Optimizado (spot + auto-hospedado) | ~$135/mes |

**Detalle:** `docs/costos-infraestructura.md`

---

## Slide 14: Lecciones Aprendidas

1. **Terraform + K8s:** La curva de aprendizaje es alta pero el resultado vale la pena
2. **Pruebas con MockBean:** KafkaTemplate<String,Object> vs <String,String> causó errores difíciles de depurar
3. **Trivy timeout:** La descarga inicial de la DB de Java (878 MB) excede el timeout por defecto
4. **Monitorización desde el inicio:** Agregar Actuator y métricas desde el día 1 simplifica la depuración
5. **Secretos nunca en YAML:** Siempre usar K8s Secrets o Vault

---

## Slide 15: Demo en Vivo

1. Login con LDAP → JWT
2. Validación QR en Gateway
3. Health checks de servicios
4. Grafana dashboard en vivo
5. Jaeger tracing de una request

---

## Slide 16: Conclusiones

- Circle Guard cumple con todos los requisitos funcionales y no funcionales
- Arquitectura de microservicios permite escalar componentes individualmente
- Privacidad y velocidad no son mutuamente excluyentes
- El sistema está listo para despliegue en producción real

---

## Slide 17: Preguntas

- **QR:** juliianavalenciia21@gmail.com
- **Repositorio:** https://github.com/JuliianaV2106/circle-guard-public
- **Kanban:** https://github.com/users/JuliianaV2106/projects/2

---

## Notas para el Presentador

| Slide | Tiempo | Notas |
|-------|--------|-------|
| 1-3 | 3 min | Introducción, no profundizar |
| 4 | 2 min | Explicar flujo de datos entre servicios |
| 5-9 | 5 min | Mostrar screenshots de Jenkins, SonarQube, ZAP |
| 10-12 | 3 min | Mostrar Grafana y Jaeger en vivo |
| 13 | 1 min | Mencionar costos evitados |
| 14-15 | 3 min | Demo en vivo (preparar terminal) |
| 16-17 | 2 min | Cierre + preguntas |

**Preparación previa:**
1. Tener Docker Desktop corriendo con todos los servicios
2. Tener Jenkins, Grafana, Prometheus abiertos en pestañas del navegador
3. Preparar comandos curl en un script para la demo
4. Verificar que los pipelines estén verdes
