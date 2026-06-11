# Epic 1 — Infraestructura como Código

## INFRA-1: Configurar Terraform para namespace DEV en Kubernetes
| Campo | Valor |
|-------|-------|
| **Issue** | [#2](https://github.com/JuliianaV2106/circle-guard-public/issues/2) |
| **Historia** | Como DevOps, quiero configurar Terraform para el namespace DEV |
| **Criterios de aceptación** | `terraform plan` exitoso; namespace `circleguard` creado en K8s |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commit** | `2697ebb` |

## INFRA-2: Configurar Terraform para namespace STAGE en Kubernetes
| Campo | Valor |
|-------|-------|
| **Issue** | [#3](https://github.com/JuliianaV2106/circle-guard-public/issues/3) |
| **Historia** | Como DevOps, quiero configurar Terraform para el namespace STAGE |
| **Criterios de aceptación** | `terraform plan` exitoso; namespace `circleguard-stage` creado en K8s |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commit** | `2697ebb` |

## INFRA-3: Configurar Terraform para namespace MASTER en Kubernetes
| Campo | Valor |
|-------|-------|
| **Issue** | [#4](https://github.com/JuliianaV2106/circle-guard-public/issues/4) |
| **Historia** | Como DevOps, quiero configurar Terraform para el namespace MASTER |
| **Criterios de aceptación** | `terraform plan` exitoso; namespace `circleguard-master` creado en K8s |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commit** | `2697ebb` |

## INFRA-4: Configurar backend remoto de Terraform
| Campo | Valor |
|-------|-------|
| **Issue** | [#5](https://github.com/JuliianaV2106/circle-guard-public/issues/5) |
| **Historia** | Como DevOps, quiero backend remoto para el estado de Terraform |
| **Criterios de aceptación** | Estado almacenado en Terraform Cloud, no local |
| **Prioridad** | Alta |
| **Estado** | ✅ Done |
| **Commit** | `11225b2` |
