# Circuit Breaker

| Campo | Valor |
|-------|-------|
| **Tipo** | Resiliencia |
| **Librería** | Resilience4j 2.1.0 |
| **Servicio** | auth-service |
| **Archivo** | `IdentityClient.java` |
| **HU** | PAT-1 |
| **Estado** | ✅ Implementado |

## Problema
Cuando identity-service no está disponible, auth-service realizaba llamadas que fallaban con timeout, bloqueando el hilo y degradando el rendimiento del sistema.

## Solución
El Circuit Breaker monitorea las llamadas a identity-service. Si la tasa de fallos supera el 50% en una ventana de 10 llamadas, el circuito se abre y las llamadas subsecuentes retornan inmediatamente un UUID deterministico.

## Configuración
- sliding-window-size: 10
- failure-rate-threshold: 50%
- wait-duration-in-open-state: 10s
- permitted-calls-in-half-open-state: 3
- minimum-number-of-calls: 5

## Fallback
Cuando el circuito está abierto, se genera un UUID determinístico basado en el username, permitiendo que el usuario se autentique aunque identity-service no esté disponible.
