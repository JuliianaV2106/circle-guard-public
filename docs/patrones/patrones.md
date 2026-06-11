# Patrones de Diseno — Circle Guard

## Patrones existentes en la arquitectura

### 1. API Gateway
**Servicio:** gateway-service  
**Descripcion:** El gateway-service actua como punto de entrada unico para
las solicitudes de acceso al campus. Centraliza la validacion de tokens JWT
y la consulta del estado sanitario en Redis, evitando que cada servicio
implemente esta logica individualmente.  
**Beneficio:** Simplifica el cliente, centraliza la seguridad y reduce
el acoplamiento entre servicios.

### 2. Strangler Fig
**Descripcion:** La arquitectura permite reemplazar servicios individuales
sin afectar al resto del sistema. Por ejemplo, el identity-service puede
ser reemplazado por una implementacion diferente sin modificar auth-service,
siempre que mantenga el mismo contrato de API.  
**Beneficio:** Facilita la evolucion incremental del sistema.

### 3. Database per Service
**Descripcion:** Cada microservicio tiene su propia base de datos aislada.
auth-service usa `circleguard_auth`, identity-service usa `circleguard_identity`,
form-service usa `circleguard_form`. Ninguna base de datos es compartida.  
**Beneficio:** Independencia de despliegue y escalado por servicio.

### 4. Event-Driven Architecture
**Servicio:** notification-service, promotion-service  
**Descripcion:** Los servicios se comunican mediante eventos en Apache Kafka.
Cuando el promotion-service cambia el estado sanitario de un usuario, publica
un evento que el notification-service consume para enviar alertas.  
**Beneficio:** Desacoplamiento temporal entre servicios, mayor resiliencia.

### 5. Anonymization Pattern
**Servicios:** auth-service, identity-service  
**Descripcion:** Las identidades reales de los usuarios nunca se almacenan
en el grafo de contactos. El identity-service mapea identidades reales a
UUIDs anonimos que son los que circulan por el resto del sistema.  
**Beneficio:** Cumplimiento de FERPA, privacidad por diseno.

---

## Patrones implementados en este proyecto

### 6. Circuit Breaker
**Servicio:** auth-service  
**Libreria:** Resilience4j 2.1.0  
**Archivo:** `services/circleguard-auth-service/src/main/java/com/circleguard/auth/client/IdentityClient.java`

**Problema que resuelve:** Cuando identity-service no esta disponible,
auth-service realizaba llamadas que fallaban con timeout, bloqueando
el hilo y degradando el rendimiento del sistema.

**Solucion implementada:** El Circuit Breaker monitorea las llamadas a
identity-service. Si la tasa de fallos supera el 50% en una ventana de
10 llamadas, el circuito se abre y las llamadas subsecuentes retornan
inmediatamente un UUID deterministico basado en el username, sin llamar
a identity-service.

**Configuracion:**

| Parametro | Valor | Descripcion |
|-----------|-------|-------------|
| sliding-window-size | 10 | Numero de llamadas para calcular tasa de fallos |
| failure-rate-threshold | 50% | Tasa de fallos para abrir el circuito |
| wait-duration-in-open-state | 10s | Tiempo en estado abierto antes de pasar a semi-abierto |
| permitted-calls-in-half-open | 3 | Llamadas de prueba en estado semi-abierto |
| minimum-number-of-calls | 5 | Minimo de llamadas antes de calcular tasa |
| slow-call-duration-threshold | 2s | Tiempo maximo antes de considerar llamada lenta |
| timeout-duration | 3s | Timeout maximo por llamada |

**Estados del Circuit Breaker:**

El circuito comienza en estado CLOSED (funcionamiento normal). Cuando
la tasa de fallos supera el 50% en 10 llamadas, pasa a OPEN y el
fallback retorna un UUID deterministico. Despues de 10 segundos pasa
a HALF-OPEN y permite 3 llamadas de prueba. Si estas tienen exito,
vuelve a CLOSED. Si fallan, vuelve a OPEN.

**Fallback implementado:**

Cuando el circuito esta abierto, en lugar de fallar la autenticacion
completamente, se genera un UUID deterministico basado en el username
del usuario usando `UUID.nameUUIDFromBytes`. Esto permite que el usuario
se autentique aunque identity-service no este disponible.

**Beneficio:** El sistema de autenticacion es resiliente a fallos de
identity-service, evitando fallos en cascada.

---

### 7. External Configuration
**Herramienta:** Kubernetes ConfigMaps gestionados con Terraform  
**Archivos:** `terraform/environments/*/main.tf`

**Problema que resuelve:** La configuracion de los servicios estaba
embebida en las imagenes Docker o en archivos de propiedades del
repositorio, lo que requeria reconstruir las imagenes para cambiar
la configuracion.

**Solucion implementada:** Toda la configuracion de infraestructura
se externaliza en ConfigMaps de Kubernetes gestionados por Terraform.
Cada ambiente tiene su propio ConfigMap con valores especificos.

**Configuracion externalizada por ambiente:**

| Variable | DEV | STAGE | MASTER |
|---------|-----|-------|--------|
| SPRING_PROFILES_ACTIVE | dev | stage | prod |
| LOG_LEVEL | DEBUG | INFO | WARN |
| POSTGRES_HOST | host.docker.internal | host.docker.internal | host.docker.internal |
| REDIS_HOST | host.docker.internal | host.docker.internal | host.docker.internal |

**Beneficio:** Cambiar la configuracion no requiere reconstruir imagenes.
La configuracion es trazable via control de versiones de Terraform.

---

### 8. Health Check
**Herramienta:** Kubernetes liveness y readiness probes  
**Archivos:** `terraform/modules/microservice/main.tf`

**Problema que resuelve:** Kubernetes no sabia cuando un pod estaba
listo para recibir trafico o cuando habia entrado en un estado
irrecuperable, causando que el trafico llegara a pods no disponibles.

**Solucion implementada:** Se configuran liveness probes (el pod esta
vivo) y readiness probes (el pod esta listo para recibir trafico)
usando el endpoint `/actuator/health` de Spring Boot Actuator, gestionadas
por el modulo `microservice` de Terraform.

**Beneficio:** Kubernetes reinicia automaticamente pods en estado
irrecuperable y no envia trafico a pods que aun no estan listos.

---

### 9. Retry Pattern
**Implementaciones:** Resilience4j Retry (auth-service), Spring Retry (notification-service)

#### Resilience4j Retry — auth-service
**Archivo:** `services/circleguard-auth-service/src/main/java/com/circleguard/auth/client/IdentityClient.java`

**Problema que resuelve:** Cuando identity-service esta temporalmente
caído o sobrecargado, las llamadas fallaban inmediatamente sin reintento,
causando falsos positivos de autenticacion.

**Solucion implementada:** Se agrega `@Retry` de Resilience4j sobre el
mismo `IdentityClient` que ya tenia Circuit Breaker. La ejecucion es:
Retry (3 intentos con 1s de espera) → Circuit Breaker → fallback.

**Configuracion:**

| Parametro | Valor | Descripcion |
|-----------|-------|-------------|
| max-attempts | 3 | Intentos maximos antes de fallback |
| wait-duration | 1s | Espera entre reintentos |
| retry-exceptions | ResourceAccessException, TimeoutException | Excepciones que disparan retry |

#### Spring Retry — notification-service
**Archivos:**
- `services/circleguard-notification-service/src/main/java/com/circleguard/notification/service/PushServiceImpl.java`
- `services/circleguard-notification-service/src/main/java/com/circleguard/notification/service/EmailServiceImpl.java`
- `services/circleguard-notification-service/src/main/java/com/circleguard/notification/service/SmsServiceImpl.java`

**Problema que resuelve:** El envio de notificaciones (push, email, SMS)
falla por problemas temporales de red o del proveedor externo (Gotify,
servidor SMTP, gateway SMS).

**Solucion implementada:** `@Retryable` de Spring Retry con 3 intentos
y backoff de 2 segundos. Si todos fallan, `@Recover` registra el fallo
definitivo en el log de auditoria.

**Configuracion comun:**

| Parametro | Valor | Descripcion |
|-----------|-------|-------------|
| maxAttempts | 3 | Reintentos maximos |
| backoff | 2000ms | Espera entre reintentos |
| retryFor | Exception.class | Cualquier excepcion dispara retry |

**Beneficio:** Las notificaciones se reintentan automaticamente ante
fallos transitorios, mejorando la tasa de entrega sin intervencion manual.

---

## Resumen de patrones

| # | Patron | Tipo | Estado |
|---|--------|------|--------|
| 1 | API Gateway | Arquitectura | Existente |
| 2 | Strangler Fig | Arquitectura | Existente |
| 3 | Database per Service | Datos | Existente |
| 4 | Event-Driven Architecture | Mensajeria | Existente |
| 5 | Anonymization | Seguridad/Privacidad | Existente |
| 6 | Circuit Breaker | Resiliencia | Implementado |
| 7 | External Configuration | Configuracion | Implementado |
| 8 | Health Check | Disponibilidad | Implementado |
| 9 | Retry | Resiliencia | Implementado |

---
*Documentacion generada como parte del Proyecto Final — Ingenieria de Software V*
