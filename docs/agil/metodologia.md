# Metodologia Agil — Circle Guard

## Marco de trabajo

Circle Guard utiliza **Kanban** como metodologia agil principal, complementado
con **GitFlow** como estrategia de branching. Esta combinacion permite un flujo
continuo de entrega sin la rigidez de sprints fijos, adecuado para un proyecto
de infraestructura y microservicios donde las tareas tienen duraciones variables.

## Tablero Kanban

**Herramienta:** GitHub Projects  
**Repositorio:** https://github.com/JuliianaV2106/circle-guard-public  
**URL del tablero:** https://github.com/JuliianaV2106/circle-guard-public/projects

### Columnas definidas

| Columna | Descripcion | Criterio de entrada |
|---------|-------------|---------------------|
| Backlog | Todas las tareas identificadas del proyecto | Tarea definida con titulo y descripcion |
| Todo | Tareas priorizadas para la iteracion actual | Tarea con criterios de aceptacion definidos |
| In Progress | Tareas en desarrollo activo | Rama feature/* creada en el repositorio |
| In Review | Tareas completadas pendientes de revision | Pull Request abierto hacia develop |
| Done | Tareas completadas y mergeadas | PR mergeado y funcionalidad verificada |

### Epics e historias de usuario

#### Epic 1 - Infraestructura como Codigo
| ID | Historia | Criterios de aceptacion |
|----|---------|------------------------|
| INFRA-1 | Como DevOps, quiero configurar Terraform para el namespace DEV para tener infraestructura reproducible | Terraform plan ejecuta sin errores, namespace circleguard existe en K8s |
| INFRA-2 | Como DevOps, quiero configurar Terraform para el namespace STAGE | Terraform plan ejecuta sin errores, namespace circleguard-stage existe |
| INFRA-3 | Como DevOps, quiero configurar Terraform para el namespace MASTER | Terraform plan ejecuta sin errores, namespace circleguard-master existe |
| INFRA-4 | Como DevOps, quiero un backend remoto para el estado de Terraform | Estado almacenado remotamente, no en el repositorio |

#### Epic 2 - CI/CD Avanzado
| ID | Historia | Criterios de aceptacion |
|----|---------|------------------------|
| CICD-1 | Como desarrollador, quiero recibir notificaciones cuando un pipeline falla | Notificacion llega en menos de 2 minutos del fallo |
| CICD-2 | Como lider tecnico, quiero aprobar manualmente los despliegues a produccion | Pipeline se detiene esperando aprobacion antes de deploy a MASTER |
| CICD-3 | Como desarrollador, quiero ver el reporte de cobertura de codigo | Reporte JaCoCo disponible como artefacto en Jenkins |

#### Epic 3 - Pruebas Completas
| ID | Historia | Criterios de aceptacion |
|----|---------|------------------------|
| TEST-1 | Como equipo de seguridad, quiero escaneo automatico con OWASP ZAP | Reporte ZAP generado como artefacto en pipeline |
| TEST-2 | Como desarrollador, quiero ver la cobertura de codigo por servicio | Cobertura minima del 70% en servicios principales |

#### Epic 4 - Observabilidad
| ID | Historia | Criterios de aceptacion |
|----|---------|------------------------|
| OBS-1 | Como operador, quiero dashboards de metricas en Grafana | Dashboard con CPU, memoria y latencia por servicio |
| OBS-2 | Como operador, quiero buscar logs centralizados en Kibana | Logs de todos los servicios disponibles en Kibana |
| OBS-3 | Como operador, quiero alertas cuando un servicio falla | Alerta disparada en menos de 1 minuto de fallo |

#### Epic 5 - Seguridad
| ID | Historia | Criterios de aceptacion |
|----|---------|------------------------|
| SEC-1 | Como administrador, quiero RBAC en Kubernetes | Roles y bindings definidos para cada namespace |
| SEC-2 | Como desarrollador, quiero secretos gestionados de forma segura | Secretos en Kubernetes Secrets, no en archivos planos |

#### Epic 6 - Patrones de Diseno
| ID | Historia | Criterios de aceptacion |
|----|---------|------------------------|
| PAT-1 | Como arquitecto, quiero Circuit Breaker en las llamadas entre servicios | Circuit breaker abre despues de 5 fallos consecutivos |
| PAT-2 | Como arquitecto, quiero configuracion externalizada | Configuracion en ConfigMaps de Kubernetes |

## Estrategia de Branching — GitFlow

### Ramas principales

| Rama | Proposito | Proteccion |
|------|-----------|------------|
| `master` | Codigo en produccion | Requiere PR + aprobacion |
| `develop` | Integracion de features | Requiere PR |
| `release/x.x.x` | Preparacion de releases | Requiere PR hacia master |

### Ramas de trabajo

| Rama | Patron | Ejemplo |
|------|--------|---------|
| Feature | `feature/EPIC-ID-descripcion` | `feature/INFRA-1-terraform-dev` |
| Hotfix | `hotfix/descripcion` | `hotfix/fix-postgres-auth` |

### Flujo de trabajo

1. El desarrollador crea una rama `feature/EPIC-ID-descripcion` desde `develop`
2. Al terminar, abre un Pull Request hacia `develop`
3. El pipeline verifica que las pruebas pasen
4. Un revisor aprueba el PR y se mergea a `develop`
5. Cuando hay suficientes features, se crea una rama `release/x.x.x` desde `develop`
6. En la rama release se ajusta la version y se generan release notes
7. Se abre PR de `release/x.x.x` hacia `master`
8. Se aprueba, mergea y el pipeline MASTER despliega automaticamente a produccion
9. Se mergea tambien de vuelta a `develop` para mantener sincronizacion

### Reglas de proteccion de ramas

- `master`: requiere 1 aprobacion en PR, pipeline verde obligatorio
- `develop`: pipeline verde obligatorio antes de merge
- Commits directos a `master` y `develop` deshabilitados

## Iteraciones completadas

### Iteracion 1 — Pipeline CI/CD base (Taller 2)
**Periodo:** Junio 2026  
**Objetivo:** Pipeline funcional con DEV, STAGE y MASTER

**Completado:**
- Jenkins configurado con imagen personalizada
- Pipeline DEV con 23 pruebas automatizadas
- SonarQube con Quality Gate
- Trivy para escaneo de vulnerabilidades
- Pipeline STAGE con smoke tests
- Pipeline MASTER con Release Notes automaticas
- Despliegue en Kubernetes con 3 namespaces

### Iteracion 2 — Infraestructura como Codigo y Observabilidad (Proyecto Final)
**Periodo:** Junio 2026  
**Objetivo:** Infraestructura completa con Terraform y observabilidad

**En progreso:**
- Terraform para los 3 ambientes
- Prometheus y Grafana
- ELK Stack
- OWASP ZAP
- Circuit Breaker
- RBAC y secretos

## Sprints

### Sprint 1 — Infraestructura base CI/CD
**Periodo:** 1 Junio 2026 - 7 Junio 2026  
**Objetivo:** Pipeline funcional con los 3 ambientes

**Historias completadas:**

| ID | Historia | Puntos | Estado |
|----|---------|--------|--------|
| CICD-1 | Configurar Jenkins con Docker y Kubernetes | 5 | Completado |
| CICD-2 | Pipeline DEV con build y pruebas | 8 | Completado |
| CICD-3 | Integracion SonarQube | 3 | Completado |
| CICD-4 | Escaneo Trivy en imagenes Docker | 3 | Completado |
| CICD-5 | Pipeline STAGE con smoke tests | 5 | Completado |
| CICD-6 | Pipeline MASTER con Release Notes | 8 | Completado |

**Velocidad del sprint:** 32 puntos  
**Completado:** 32/32 puntos (100%)

**Retrospectiva:**
- Lo que salio bien: pipelines funcionando en los 3 ambientes, 23 pruebas automatizadas pasando
- Lo que mejorar: el auth-service no arranca localmente por configuracion de PostgreSQL
- Accion de mejora: agregar infraestructura completa con Terraform en el siguiente sprint

---

### Sprint 2 — Infraestructura como Codigo y Patrones
**Periodo:** 7 Junio 2026 - 14 Junio 2026  
**Objetivo:** Terraform para los 3 ambientes y patrones de diseno

**Historias completadas:**

| ID | Historia | Puntos | Estado |
|----|---------|--------|--------|
| INFRA-1 | Terraform namespace DEV | 3 | Completado |
| INFRA-2 | Terraform namespace STAGE | 2 | Completado |
| INFRA-3 | Terraform namespace MASTER | 2 | Completado |
| INFRA-4 | ConfigMaps por ambiente con Terraform | 5 | Completado |

**En progreso:**

| ID | Historia | Puntos | Estado |
|----|---------|--------|--------|
| INFRA-5 | Backend remoto Terraform | 5 | En progreso |
| PAT-1 | Circuit Breaker en gateway-service | 8 | En progreso |
| PAT-2 | Health checks en manifiestos K8s | 3 | En progreso |
| OBS-1 | Prometheus y Grafana | 8 | Pendiente |
| OBS-2 | ELK Stack | 8 | Pendiente |
| SEC-1 | RBAC en Kubernetes | 5 | Pendiente |
| TEST-1 | OWASP ZAP | 5 | Pendiente |

**Velocidad proyectada:** 50 puntos

## Metricas del equipo

| Metrica | Iteracion 1 | Iteracion 2 |
|---------|------------|------------|
| Tareas completadas | 16 | En progreso |
| Pruebas implementadas | 23 | Por definir |
| Cobertura de codigo | Por medir | Objetivo 70% |
| Pipelines exitosos | 3 | Por definir |

--- 
*Juliana Filigrana Valencia - Juan Manuel Casanova Marin — Junio 2026*