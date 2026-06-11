# Health Check

| Campo | Valor |
|-------|-------|
| **Tipo** | Disponibilidad |
| **Herramienta** | Kubernetes liveness/readiness probes |
| **Módulo** | `terraform/modules/microservice/` |
| **Estado** | ✅ Implementado |

## Problema
Kubernetes no sabía cuando un pod estaba listo para recibir tráfico o cuando había entrado en un estado irrecuperable.

## Solución
Se configuran liveness probes (el pod está vivo) y readiness probes (el pod está listo para recibir tráfico) usando el endpoint `/actuator/health` de Spring Boot Actuator.

## Configuración (gateway-service)
- Liveness: path=/actuator/health/liveness, initialDelay=60s, period=15s
- Readiness: path=/actuator/health/readiness, initialDelay=30s, period=10s

## Beneficio
Kubernetes reinicia automáticamente pods en estado irrecuperable y no envía tráfico a pods que aún no están listos.
