# Epic 6 — Patrones de Diseño

## PAT-1: Implementar Circuit Breaker
| Campo | Valor |
|-------|-------|
| **Issue** | [#16](https://github.com/JuliianaV2106/circle-guard-public/issues/16) |
| **Historia** | Como arquitecto, quiero Circuit Breaker en llamadas entre servicios |
| **Criterios de aceptación** | Circuit breaker abre tras 5 fallos consecutivos; llamadas fallan rápido |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commit** | `ec32997` |

## PAT-2: Implementar External Configuration
| Campo | Valor |
|-------|-------|
| **Issue** | [#17](https://github.com/JuliianaV2106/circle-guard-public/issues/17) |
| **Historia** | Como arquitecto, quiero configuración externalizada por ambiente |
| **Criterios de aceptación** | Configuración en ConfigMaps de K8s; cambios sin rebuild |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commit** | `ec32997` |

## PAT-3: Implementar Retry Pattern
| Campo | Valor |
|-------|-------|
| **Issue** | — |
| **Historia** | Como arquitecto, quiero reintentos automáticos en llamadas a servicios |
| **Criterios de aceptación** | Reintentos configurados en auth-service (Resilience4j) y notification-service (Spring Retry); fallback documentado |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commits** | `ec32997` (Spring Retry existente), commit actual (Resilience4j Retry) |
