# Epic 3 — Pruebas Completas

## TEST-1: Implementar pruebas de seguridad con OWASP ZAP
| Campo | Valor |
|-------|-------|
| **Issue** | [#9](https://github.com/JuliianaV2106/circle-guard-public/issues/9) |
| **Historia** | Como equipo de seguridad, quiero escaneo automático con OWASP ZAP |
| **Criterios de aceptación** | Escaneo ZAP ejecutado en pipeline; reporte generado como artefacto |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Pipeline** | Jenkins — stage `OWASP ZAP Scan` en DEV, STAGE, MASTER |
| **Script** | `scripts/zap-scan.sh` |

## TEST-2: Configurar reporte de cobertura con JaCoCo
| Campo | Valor |
|-------|-------|
| **Issue** | [#10](https://github.com/JuliianaV2106/circle-guard-public/issues/10) |
| **Historia** | Como desarrollador, quiero reportes de cobertura por servicio |
| **Criterios de aceptación** | Cobertura mínima 70% en servicios principales; reporte JaCoCo disponible |
| **Prioridad** | Media |
| **Estado** | ✅ Done |
| **Configuración** | `build.gradle.kts` — plugin jacoco + jacocoTestReport + jacocoTestCoverageVerification |
| **Pipeline** | Jenkins — stage `Coverage Report` |
