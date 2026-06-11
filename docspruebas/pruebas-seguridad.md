# Pruebas de Seguridad

## OWASP ZAP

Se ejecuta un escaneo ZAP Baseline contra el gateway-service.

### Ejecución manual

```bash
./scripts/zap-scan.sh http://localhost:31449 ./build/reports/zap
```

### Pipeline

El escaneo se ejecuta automáticamente en Jenkins:
- **Jenkinsfile** — stage `OWASP ZAP Scan` (DEV)
- **Jenkinsfile.stage** — stage `OWASP ZAP Scan` (STAGE)
- **Jenkinsfile.master** — stage `OWASP ZAP Scan` (STAGE)

### Reportes

Disponibles como artefactos de Jenkins y HTML publicados.

## Trivy (imágenes Docker)

Escaneo de vulnerabilidades en imágenes de contenedores:
- **Severidad:** HIGH, CRITICAL
- **Frecuencia:** Cada build
- **Exit code:** 0 (no bloquea, solo informa)
