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

> 📋 Cada HU tiene su ficha detallada en [`docsagil/`](../../docsagil/).  
> 🔗 Issues vinculados: https://github.com/JuliianaV2106/circle-guard-public/issues

#### Epic 1 — Infraestructura como Código
| ID | Historia | Issue | Estado |
|----|----------|-------|--------|
| INFRA-1 | Configurar Terraform para namespace DEV en Kubernetes | [#2](https://github.com/JuliianaV2106/circle-guard-public/issues/2) | ✅ |
| INFRA-2 | Configurar Terraform para namespace STAGE en Kubernetes | [#3](https://github.com/JuliianaV2106/circle-guard-public/issues/3) | ✅ |
| INFRA-3 | Configurar Terraform para namespace MASTER en Kubernetes | [#4](https://github.com/JuliianaV2106/circle-guard-public/issues/4) | ✅ |
| INFRA-4 | Configurar backend remoto de Terraform | [#5](https://github.com/JuliianaV2106/circle-guard-public/issues/5) | ✅ |

#### Epic 2 — CI/CD Avanzado
| ID | Historia | Issue | Estado |
|----|----------|-------|--------|
| CICD-1 | Agregar notificaciones de fallo en pipelines | [#6](https://github.com/JuliianaV2106/circle-guard-public/issues/6) | ✅ |
| CICD-2 | Implementar aprobación manual para deploy a producción | [#7](https://github.com/JuliianaV2106/circle-guard-public/issues/7) | ✅ |
| CICD-3 | Agregar reporte de cobertura de código | [#8](https://github.com/JuliianaV2106/circle-guard-public/issues/8) | ⏳ |

#### Epic 3 — Pruebas Completas
| ID | Historia | Issue | Estado |
|----|----------|-------|--------|
| TEST-1 | Implementar pruebas de seguridad con OWASP ZAP | [#9](https://github.com/JuliianaV2106/circle-guard-public/issues/9) | ⏳ |
| TEST-2 | Configurar reporte de cobertura con JaCoCo | [#10](https://github.com/JuliianaV2106/circle-guard-public/issues/10) | ⏳ |

#### Epic 4 — Observabilidad
| ID | Historia | Issue | Estado |
|----|----------|-------|--------|
| OBS-1 | Implementar Prometheus y Grafana | [#11](https://github.com/JuliianaV2106/circle-guard-public/issues/11) | ⏳ |
| OBS-2 | Implementar ELK Stack | [#12](https://github.com/JuliianaV2106/circle-guard-public/issues/12) | ⏳ |
| OBS-3 | Configurar alertas críticas | [#13](https://github.com/JuliianaV2106/circle-guard-public/issues/13) | ⏳ |

#### Epic 5 — Seguridad
| ID | Historia | Issue | Estado |
|----|----------|-------|--------|
| SEC-1 | Configurar RBAC en Kubernetes | [#14](https://github.com/JuliianaV2106/circle-guard-public/issues/14) | ⏳ |
| SEC-2 | Implementar gestión de secretos | [#15](https://github.com/JuliianaV2106/circle-guard-public/issues/15) | ⏳ |

#### Epic 6 — Patrones de Diseño
| ID | Historia | Issue | Estado |
|----|----------|-------|--------|
| PAT-1 | Implementar Circuit Breaker | [#16](https://github.com/JuliianaV2106/circle-guard-public/issues/16) | ✅ |
| PAT-2 | Implementar External Configuration | [#17](https://github.com/JuliianaV2106/circle-guard-public/issues/17) | ✅ |

_Leyenda: ✅ Done · ⏳ Pending_

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

### Iteracion 2 — Infraestructura como Codigo, CI/CD y Patrones (Proyecto Final)
**Periodo:** Junio 2026  
**Objetivo:** Infraestructura como código, CI/CD completo y patrones de diseño

**Completado:**
- ✅ INFRA-1, INFRA-2, INFRA-3 — Terraform namespaces DEV/STAGE/MASTER
- ✅ INFRA-4 — Backend remoto Terraform
- ✅ Módulo `microservice` (Deployment + Service) en Terraform
- ✅ 6 microservicios migrados a Terraform en DEV (auth, gateway, identity, form, notification, dashboard)
- ✅ 2 microservicios migrados a Terraform en STAGE y MASTER
- ✅ Pipelines actualizados a `terraform apply` (Jenkinsfile, Jenkinsfile.stage, Jenkinsfile.master)
- ✅ Documentación en `docsinfraestructura/`
- ✅ CICD-1 — Notificaciones de fallo en pipelines
- ✅ CICD-2 — Aprobación manual para producción
- ✅ PAT-1 — Circuit Breaker
- ✅ PAT-2 — External Configuration

**Pendiente (pasa a Iteración 3):**
- ⏳ CICD-3 — Reporte de cobertura de código
- ⏳ TEST-1 — OWASP ZAP
- ⏳ TEST-2 — JaCoCo
- ⏳ OBS-1 — Prometheus y Grafana
- ⏳ OBS-2 — ELK Stack
- ⏳ OBS-3 — Alertas críticas
- ⏳ SEC-1 — RBAC
- ⏳ SEC-2 — Gestión de secretos

## Sprints

### Sprint 1 — Infraestructura base CI/CD
**Periodo:** 1 Junio 2026 - 7 Junio 2026  
**Objetivo:** Pipeline funcional con los 3 ambientes

**Historias completadas:**

| ID | Historia | Puntos | Estado |
|----|---------|--------|--------|
| CICD-1 | Agregar notificaciones de fallo en pipelines (#6) | 5 | Completado |
| CICD-2 | Implementar aprobación manual para deploy a producción (#7) | 8 | Completado |
| — | Configurar SonarQube Quality Gate | 3 | Completado |
| — | Integrar Trivy Security Scan | 3 | Completado |
| — | Pipeline STAGE con smoke tests | 5 | Completado |
| — | Pipeline MASTER con Release Notes | 8 | Completado |

**Velocidad del sprint:** 32 puntos  
**Completado:** 32/32 puntos (100%)

**Retrospectiva:**
- Lo que salió bien: pipelines funcionando en los 3 ambientes, 23 pruebas automatizadas pasando
- Lo que mejorar: el auth-service no arranca localmente por configuración de PostgreSQL
- Acción de mejora: agregar infraestructura completa con Terraform en el siguiente sprint

---

### Sprint 2 — Infraestructura como Código y Patrones
**Periodo:** 7 Junio 2026 - 14 Junio 2026  
**Objetivo:** Terraform para los 3 ambientes y patrones de diseño

**Historias completadas:**

| ID | Historia | Issue | Puntos | Estado |
|----|---------|-------|--------|--------|
| INFRA-1 | Configurar Terraform para namespace DEV en Kubernetes | [#2](https://github.com/JuliianaV2106/circle-guard-public/issues/2) | 3 | Completado |
| INFRA-2 | Configurar Terraform para namespace STAGE en Kubernetes | [#3](https://github.com/JuliianaV2106/circle-guard-public/issues/3) | 2 | Completado |
| INFRA-3 | Configurar Terraform para namespace MASTER en Kubernetes | [#4](https://github.com/JuliianaV2106/circle-guard-public/issues/4) | 2 | Completado |
| INFRA-4 | Configurar backend remoto de Terraform | [#5](https://github.com/JuliianaV2106/circle-guard-public/issues/5) | 5 | Completado |
| PAT-1 | Implementar Circuit Breaker | [#16](https://github.com/JuliianaV2106/circle-guard-public/issues/16) | 8 | Completado |
| PAT-2 | Implementar External Configuration | [#17](https://github.com/JuliianaV2106/circle-guard-public/issues/17) | 3 | Completado |

**Pendiente (pasa a Sprint 3):**

| ID | Historia | Issue | Puntos | Estado |
|----|---------|-------|--------|--------|
| CICD-3 | Agregar reporte de cobertura de código | [#8](https://github.com/JuliianaV2106/circle-guard-public/issues/8) | 5 | Pendiente |
| TEST-1 | Implementar pruebas de seguridad con OWASP ZAP | [#9](https://github.com/JuliianaV2106/circle-guard-public/issues/9) | 8 | Pendiente |
| TEST-2 | Configurar reporte de cobertura con JaCoCo | [#10](https://github.com/JuliianaV2106/circle-guard-public/issues/10) | 3 | Pendiente |
| OBS-1 | Implementar Prometheus y Grafana | [#11](https://github.com/JuliianaV2106/circle-guard-public/issues/11) | 8 | Pendiente |
| OBS-2 | Implementar ELK Stack | [#12](https://github.com/JuliianaV2106/circle-guard-public/issues/12) | 8 | Pendiente |
| OBS-3 | Configurar alertas críticas | [#13](https://github.com/JuliianaV2106/circle-guard-public/issues/13) | 5 | Pendiente |
| SEC-1 | Configurar RBAC en Kubernetes | [#14](https://github.com/JuliianaV2106/circle-guard-public/issues/14) | 5 | Pendiente |
| SEC-2 | Implementar gestión de secretos | [#15](https://github.com/JuliianaV2106/circle-guard-public/issues/15) | 3 | Pendiente |

**Velocidad del sprint:** 23 puntos  
**Completado:** 23/23 puntos (100%)

### Sprint 3 — Pendientes (Observabilidad, Seguridad, Pruebas)
**Periodo:** 14 Junio 2026 - 21 Junio 2026  
**Objetivo:** Finalizar las 8 HUs pendientes del proyecto

**Planificado:**

| ID | Historia | Issue | Puntos | Prioridad |
|----|---------|-------|--------|-----------|
| OBS-1 | Implementar Prometheus y Grafana | [#11](https://github.com/JuliianaV2106/circle-guard-public/issues/11) | 8 | Alta |
| OBS-2 | Implementar ELK Stack | [#12](https://github.com/JuliianaV2106/circle-guard-public/issues/12) | 8 | Alta |
| OBS-3 | Configurar alertas críticas | [#13](https://github.com/JuliianaV2106/circle-guard-public/issues/13) | 5 | Alta |
| SEC-1 | Configurar RBAC en Kubernetes | [#14](https://github.com/JuliianaV2106/circle-guard-public/issues/14) | 5 | Alta |
| TEST-1 | Implementar pruebas de seguridad con OWASP ZAP | [#9](https://github.com/JuliianaV2106/circle-guard-public/issues/9) | 8 | Alta |
| CICD-3 | Agregar reporte de cobertura de código | [#8](https://github.com/JuliianaV2106/circle-guard-public/issues/8) | 5 | Media |
| TEST-2 | Configurar reporte de cobertura con JaCoCo | [#10](https://github.com/JuliianaV2106/circle-guard-public/issues/10) | 3 | Media |
| SEC-2 | Implementar gestión de secretos | [#15](https://github.com/JuliianaV2106/circle-guard-public/issues/15) | 3 | Media |

## Metricas del equipo

| Metrica | Sprint 1 | Sprint 2 |
|---------|----------|----------|
| HUs completadas | 2 (CICD-1, CICD-2) | 6 (INFRA-1..4, PAT-1, PAT-2) |
| Módulos Terraform | 0 | 3 (namespace, configmap, microservice) |
| Puntos completados | 32 | 23 |
| Cobertura de código | Por medir | Objetivo 70% |
| Pipelines exitosos | 3 (DEV, STAGE, MASTER) | 3 |

--- 
*Juliana Filigrana Valencia - Juan Manuel Casanova Marin — Junio 2026*