# Change Management y Release Notes

## Versionado Semántico

El proyecto utiliza **Semantic Versioning (SemVer 2.0)**:
- **MAJOR**: Cambios incompatibles en la API
- **MINOR**: Nuevas funcionalidades compatibles hacia atrás
- **PATCH**: Correcciones de bugs compatibles hacia atrás

Formato: `vMAJOR.MINOR.PATCH` (ej: `v1.0.0`, `v1.0.1`, `v1.2.0`)

## Proceso de Release

1. El pipeline Jenkinsfile.master determina automáticamente la próxima versión
2. Lee el último tag `v*.*.*` del repositorio
3. Incrementa el componente PATCH automáticamente
4. Construye imágenes Docker con la versión semántica
5. Despliega a STAGE, ejecuta pruebas del sistema
6. Espera aprobación manual para producción
7. Despliega a MASTER
8. Genera Release Notes automáticas
9. Crea y pushea el tag `vX.Y.Z` al repositorio

## Plan de Rollback

```bash
# Rollback de deployment en Kubernetes
kubectl rollout undo deployment/gateway-service -n circleguard-master
kubectl rollout undo deployment/notification-service -n circleguard-master

# Rollback a versión específica
kubectl set image deployment/gateway-service \
  gateway-service=circleguard/gateway-service:v1.0.0 \
  -n circleguard-master
```

## Release Notes

Las Release Notes se generan automáticamente al final del pipeline
Jenkinsfile.master e incluyen:
- Versión y fecha de release
- Servicios desplegados con sus imágenes
- Estado de cada etapa del pipeline
- Últimos commits incluidos
- Vulnerabilidades conocidas
- Plan de rollback

## Etiquetado de Releases

Cada release exitosa crea un tag Git firmado:
```bash
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin v1.0.1
```
