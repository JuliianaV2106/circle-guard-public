# Change Management y Release Notes

## Versionado Semántico

El proyecto utiliza **Semantic Versioning (SemVer 2.0)**:
- **MAJOR**: Cambios incompatibles en la API
- **MINOR**: Nuevas funcionalidades compatibles hacia atrás
- **PATCH**: Correcciones de bugs compatibles hacia atrás

Formato: `vMAJOR.MINOR.PATCH` (ej: `v1.0.0`, `v1.0.1`, `v1.2.0`)

La versión actual se mantiene en `version.properties` y se actualiza automáticamente en cada release.

## Proceso de Release (Jenkinsfile.master)

1. **Checkout**: Clona rama `master`
2. **Semantic Versioning**:
   - Busca último tag `v*.*.*` en el repositorio
   - Incrementa componente PATCH automáticamente
   - Actualiza `version.properties` y lo commitea
3. **Build**: Compila todos los servicios
4. **Unit Tests**: Ejecuta pruebas unitarias
5. **Static Analysis**: SonarQube con cobertura JaCoCo (mínimo 70%)
6. **Docker Build**: Construye imágenes con tag `vX.Y.Z` y `latest`
7. **Security Scan**: Trivy (HIGH/CRITICAL) + OWASP ZAP
8. **Deploy to STAGE**: Terraform apply al namespace `circleguard-stage`
9. **System Tests**: Pruebas de integración contra STAGE
10. **Approval**: Aprobación manual para producción (timeout 30 min)
11. **Deploy to MASTER**: Terraform apply al namespace `circleguard-master`
12. **Generate Release Notes**: Script `scripts/generate-release-notes.sh`
13. **Tag Release**: Crea tag Git `vX.Y.Z` y lo pushea
14. **GitHub Release**: Crea GitHub Release con `gh` CLI desde el tag

## Gestión de Cambios (Change Management)

Ver documento completo en `docschange-management/index.md`.

### Tipos de Cambio

| Tipo | Descripción | Aprobación |
|------|-------------|-----------|
| **Normal** | Nueva funcionalidad, cambios mayores | CAB semanal |
| **Estándar** | Bugfix, config, dependencias | Pipeline CI/CD |
| **Emergencia** | Hotfix seguridad, caída servicio | Líder técnico + PO |
| **Cosmético** | Docs, logging, sin efecto runtime | Ninguna |

### Flujo CRQ

1. Crear GitHub Issue con template de Change Request
2. Clasificar tipo de cambio
3. Seguir el flujo según el tipo (CAB / Pipeline / Aprobación rápida)
4. Implementar, probar, verificar
5. Cerrar CRQ

## Plan de Rollback

```bash
# Rollback de deployment en Kubernetes
kubectl rollout undo deployment/gateway-service -n circleguard-master
kubectl rollout undo deployment/notification-service -n circleguard-master

# Rollback a versión específica
kubectl set image deployment/gateway-service \
  gateway-service=circleguard/gateway-service:v1.0.0 \
  -n circleguard-master

# Rollback via Terraform (re-aplicar versión anterior)
# terraform apply -auto-approve -var-file=master.tfvars
```

## Release Notes Automáticas

El script `scripts/generate-release-notes.sh` genera release notes en markdown incluyendo:
- Versión, fecha y rango de commits
- Cambios agrupados por tipo (feat, fix, docs, refactor, chore, test, ci)
- Lista de servicios desplegados con sus imágenes Docker
- Plan de rollback
- Información de Change Management (CRQ ID, tipo, impacto)

## Etiquetado de Releases

Cada release exitosa crea:
1. **Git tag** firmado: `git tag -a vX.Y.Z -m "Release vX.Y.Z"`
2. **GitHub Release** con `gh release create` incluyendo release notes
3. **Actualización** de `version.properties` con la nueva versión

## Change Request (CRQ) Template

Crear en GitHub Issues con el siguiente contenido:

```markdown
---
título: "[CRQ-XXX] Descripción del cambio"
fecha: YYYY-MM-DD
solicitante: Nombre
tipo: normal | estandar | emergencia | cosmetico
servicios_afectados: [lista]
riesgo: alto | medio | bajo
---

## Descripción
<!-- Qué se cambia y por qué -->

## Justificación
<!-- Beneficio esperado -->

## Servicios Afectados
- [ ] auth-service
- [ ] gateway-service
- [ ] identity-service
- [ ] form-service
- [ ] notification-service
- [ ] dashboard-service

## Plan de Pruebas
<!-- Cómo se verificará el cambio -->

## Plan de Rollback
<!-- Pasos para revertir -->

## Ventana Propuesta
<!-- Fecha y hora deseada -->
```

---

*Documento controlado — Versión 2.0 — Última actualización: 2026-06-11*
