# Pruebas Completas — CircleGuard

## Resumen

| Tipo | Servicios | Estado | Pipeline |
|------|-----------|--------|----------|
| Unitarias | auth, promotion, notification, form, identity, dashboard, file, gateway | ✅ | Jenkins — stage `Test` |
| Integración | auth ↔ identity | ✅ | Jenkins — stage `Test` |
| E2E | login → gateway → QR (auth + gateway) | ✅ | Commit `7cb74a7` |
| Rendimiento (Locust) | gateway-service (validación de tokens) | ✅ | `locustfile.py` |
| Cobertura (JaCoCo) | Todos los servicios | ✅ | Jenkins — stage `Coverage Report` |
| Seguridad (OWASP ZAP) | gateway-service | ✅ | Jenkins — stage `OWASP ZAP Scan` |

## Reportes

- **JaCoCo:** `**/build/reports/jacoco/test/html/index.html`
- **OWASP ZAP:** `build/reports/zap/zap-report.html`
- **Locust:** `locustfile.py` (ejecutar manualmente)
- **JUnit XML:** `**/build/test-results/test/*.xml`

## Cobertura Mínima

Objetivo: ≥70% en servicios principales (configurado en `build.gradle.kts` via `jacocoTestCoverageVerification`).
