# Epic 2 — CI/CD Avanzado

## CICD-1: Agregar notificaciones de fallo en pipelines
| Campo | Valor |
|-------|-------|
| **Issue** | [#6](https://github.com/JuliianaV2106/circle-guard-public/issues/6) |
| **Historia** | Como desarrollador, quiero recibir notificaciones cuando un pipeline falla |
| **Criterios de aceptación** | Email de notificación enviado en <2 min del fallo |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commits** | `40c0be6`, `75d4d78`, `366a84c` |

## CICD-2: Implementar aprobación manual para deploy a producción
| Campo | Valor |
|-------|-------|
| **Issue** | [#7](https://github.com/JuliianaV2106/circle-guard-public/issues/7) |
| **Historia** | Como líder técnico, quiero aprobar manualmente despliegues a producción |
| **Criterios de aceptación** | Pipeline se detiene en etapa de aprobación antes de MASTER |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commit** | `40c0be6` |

## CICD-3: Agregar reporte de cobertura de código
| Campo | Valor |
|-------|-------|
| **Issue** | [#8](https://github.com/JuliianaV2106/circle-guard-public/issues/8) |
| **Historia** | Como desarrollador, quiero ver reportes de cobertura en el pipeline |
| **Criterios de aceptación** | Reporte JaCoCo disponible como artefacto en Jenkins |
| **Prioridad** | Media |
| **Estado** | ⏳ Pending |

## CICD-4: Versionado semántico automático
| Campo | Valor |
|-------|-------|
| **Issue** | — |
| **Historia** | Como DevOps, quiero versionado semántico automático en los releases |
| **Criterios de aceptación** | Tag vMAJOR.MINOR.PATCH generado automáticamente; imágenes Docker etiquetadas con versión semántica |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Pipeline** | Jenkinsfile.master — stage `Semantic Versioning` + `Tag Release` |
