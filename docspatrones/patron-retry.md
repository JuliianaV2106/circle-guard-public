# Retry Pattern

| Campo | Valor |
|-------|-------|
| **Tipo** | Resiliencia |
| **Implementaciones** | Resilience4j Retry (auth-service), Spring Retry (notification-service) |
| **Estado** | ✅ Implementado |

## Problema
Llamadas a servicios externos o internos fallan por problemas transitorios de red o sobrecarga. Sin reintentos, estos fallos causan falsos positivos y degradan la experiencia.

## Solución

### Resilience4j Retry (auth-service)
`IdentityClient.java` — 3 reintentos con 1s de espera ante `ResourceAccessException` o `TimeoutException`. Se ejecuta antes del Circuit Breaker en la cadena de resiliencia.

### Spring Retry (notification-service)
`PushServiceImpl`, `EmailServiceImpl`, `SmsServiceImpl` — 3 reintentos con backoff de 2s ante cualquier excepción. Método `@Recover` registra el fallo definitivo.

## Configuración
| Implementación | maxAttempts | wait/backoff | retryFor |
|----------------|-------------|--------------|----------|
| Resilience4j | 3 | 1s | ResourceAccessException, TimeoutException |
| Spring Retry | 3 | 2s | Exception.class |

## Beneficio
Las operaciones fallidas se reintentan automáticamente ante fallos transitorios, mejorando la tasa de éxito sin intervención manual.
