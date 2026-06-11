# External Configuration

| Campo | Valor |
|-------|-------|
| **Tipo** | Configuración |
| **Herramienta** | Kubernetes ConfigMaps + Terraform |
| **HU** | PAT-2 |
| **Estado** | ✅ Implementado |

## Problema
La configuración de los servicios estaba embebida en las imágenes Docker, requiriendo reconstruir las imágenes para cambiar cualquier parámetro.

## Solución
Toda la configuración se externaliza en ConfigMaps de Kubernetes gestionados por Terraform. Cada ambiente tiene su propio ConfigMap con valores específicos.

## Configuración por ambiente
| Variable | DEV | STAGE | MASTER |
|----------|-----|-------|--------|
| SPRING_PROFILES_ACTIVE | dev | stage | prod |
| LOG_LEVEL | DEBUG | INFO | WARN |

## Beneficio
Cambiar la configuración no requiere reconstruir imágenes. La configuración es trazable via control de versiones de Terraform.
