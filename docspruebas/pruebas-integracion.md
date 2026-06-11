# Pruebas de Integración

## auth-service → identity-service

```java
// IdentityClient llama a identity-service REST API
// Circuit Breaker + Retry para resiliencia
getAnonymousId(realIdentity) -> HTTP POST -> identity-service
```

## Cobertura

- Commit `0432b6b` — integración real entre auth-service e identity-service
- WireMock para simular respuestas del identity-service en tests unitarios
