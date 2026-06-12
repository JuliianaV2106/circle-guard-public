# Organización del Repositorio Git

## Estructura de Ramas

```
main (protección sanitaria)
  ↑
  master (producción)
    ↑
    release/1.0.0
      ↑
      develop (integración)
        ↑
        feature/* (ramas de funcionalidad)
        fix/* (ramas de corrección)
        docs/* (ramas de documentación)
```

| Rama | Propósito | Pipeline | Despliegue |
|------|-----------|----------|------------|
| `main` | Rama protegida, solo lectura | Ninguno | N/A |
| `master` | Producción | Jenkinsfile.master | Namespace `circleguard-master` via Terraform |
| `release/*` | Preparación de release | Manual | Stage para validación |
| `develop` | Integración continua | Jenkinsfile | Namespace `circleguard-dev` via Terraform |
| `feature/*` | Desarrollo de funcionalidades | Ninguno | Local |
| `fix/*` | Corrección de bugs | Ninguno | Local |
| `docs/*` | Documentación | Ninguno | N/A |

## Convención de Commits

Usamos **Conventional Commits** para generar release notes automáticas:

```
<tipo>: <descripción>

Tipos: feat, fix, docs, refactor, chore, test, ci, style, perf
```

Ejemplos:
```
feat: add QR validation endpoint to gateway service
fix: correct Kafka consumer group id in notification service
docs: update architecture diagram with new services
chore: update dependencies to latest patch versions
```

## Versionado Semántico

Formato: `vMAJOR.MINOR.PATCH`

- **MAJOR**: Cambios incompatibles en API
- **MINOR**: Nuevas funcionalidades compatibles
- **PATCH**: Correcciones de bugs

El componente PATCH se incrementa automáticamente en el pipeline `Jenkinsfile.master`.

## Tags

Cada release a `master` crea un tag Git firmado y un GitHub Release:

```
v1.0.0 → v1.0.1 → v1.0.2 → ...
```

Los tags se generan automáticamente en el stage `Tag Release` del pipeline master.

## Archivos clave del repositorio

| Archivo | Propósito |
|---------|-----------|
| `build.gradle.kts` | Build system con plugins de calidad (JaCoCo, Sonar, Dependency-Check) |
| `settings.gradle.kts` | Definición de módulos (8 microservicios) |
| `version.properties` | Versión actual del proyecto |
| `Jenkinsfile` | Pipeline CI/CD para develop |
| `Jenkinsfile.stage` | Pipeline de despliegue a stage |
| `Jenkinsfile.master` | Pipeline de release a producción |
| `Dockerfile.*` | Dockerfiles para cada microservicio (8) |
| `docker-compose.*.yml` | Orquestación local |
| `terraform/` | Infraestructura como código (3 entornos) |
| `config/` | Configuración de monitoreo (Prometheus, Grafana, ELK) |
| `scripts/` | Scripts de utilidad (ZAP, release notes, certs, secrets) |
| `docs*/` | Documentación del proyecto |

## Políticas del repositorio

1. **No hacer push directo a `master`** — solo mediante PR desde `develop` o `release/*`
2. **Los commits deben tener mensajes descriptivos** siguiendo Conventional Commits
3. **No committear secretos** — usar Kubernetes Secrets + Terraform sensitive variables
4. **Los tags de release solo los crea el pipeline** — no crear tags manualmente
5. **Mantener `develop` siempre en estado deployable** — pruebas deben pasar antes del merge
