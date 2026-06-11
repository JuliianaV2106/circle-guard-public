# Pruebas Unitarias

## Microservicios con pruebas

| Servicio | Framework | Tests |
|----------|-----------|-------|
| auth-service | JUnit 5 + Spring Boot Test | Login, validación JWT, LDAP |
| gateway-service | JUnit 5 + WireMock | Validación de tokens, manejo de errores |
| identity-service | JUnit 5 | Mapeo de identidades |
| notification-service | JUnit 5 + Testcontainers | Push, email, SMS |
| form-service | JUnit 5 | CRUD formularios |
| dashboard-service | JUnit 5 | Reportes y métricas |
| file-service | JUnit 5 | Subida/descarga de archivos |
| promotion-service | JUnit 5 + Testcontainers + H2 | Promociones, health status |

## Ejecución

```bash
./gradlew test
```
