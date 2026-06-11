# Pruebas de Rendimiento (Locust)

## Escenario

Simula usuarios validando tokens contra el gateway-service.

## Endpoints probados

| Endpoint | Peso | Descripción |
|----------|------|-------------|
| POST /api/v1/gate/validate [token válido] | 3/6 | Escenario feliz |
| POST /api/v1/gate/validate [token inválido] | 2/6 | Error esperado |
| POST /api/v1/gate/validate [token vacío] | 1/6 | Edge case |

## Ejecución

```bash
# Instalar Locust
pip install locust

# Ejecutar
locust -f locustfile.py --host=http://localhost:31449

# Headless con 100 usuarios
locust -f locustfile.py --headless -u 100 -r 10 --run-time 30s
```

## Resultados esperados
- Throughput: >50 requests/segundo
- Latencia p99: <500ms
- Tasa de error: <1%

## Pruebas de performance (promotion-service)

El promotion-service incluye pruebas de rendimiento con Testcontainers
y Redis que verifican tiempos de respuesta bajo carga.
