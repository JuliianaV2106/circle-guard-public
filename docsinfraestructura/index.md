# Infraestructura como Código — CircleGuard

## Estructura de Terraform

```
terraform/
├── modules/
│   ├── namespace/         # Crea namespaces de Kubernetes
│   ├── configmap/         # Crea ConfigMaps con configuración
│   └── microservice/      # Crea Deployment + Service para un microservicio
└── environments/
    ├── dev/               # Namespace: circleguard
    │   ├── main.tf        # 6 microservicios (auth, gateway, identity, form, notification, dashboard)
    │   ├── variables.tf
    │   ├── dev.tfvars
    │   └── outputs.tf
    ├── stage/             # Namespace: circleguard-stage
    │   ├── main.tf        # 2 microservicios (gateway, notification)
    │   ├── variables.tf
    │   ├── stage.tfvars
    │   └── outputs.tf
    └── master/            # Namespace: circleguard-master
        ├── main.tf        # 2 microservicios (gateway, notification)
        ├── variables.tf
        ├── master.tfvars
        └── outputs.tf
```

## Módulos

| Módulo | Recursos | Propósito |
|--------|----------|-----------|
| `namespace` | `kubernetes_namespace` | Crear namespace con labels estándar |
| `configmap` | `kubernetes_config_map` | Configuración externalizada por ambiente |
| `microservice` | `kubernetes_deployment` + `kubernetes_service` | Desplegar un microservicio completo |

## Ambientes

| Ambiente | Namespace | Microservicios | Perfil Spring |
|----------|-----------|----------------|---------------|
| DEV | `circleguard` | 6 (auth, gateway, identity, form, notification, dashboard) | `dev` |
| STAGE | `circleguard-stage` | 2 (gateway, notification) | `stage` |
| MASTER | `circleguard-master` | 2 (gateway, notification) | `prod` |

## Backend Remoto

El estado de Terraform se almacena en **Terraform Cloud** (organización `circle-guard-juliana`), con 3 workspaces independientes:
- `circle-guard-dev`
- `circle-guard-stage`
- `circle-guard-master`

## Diagrama de Arquitectura

Ver [docs/arquitectura/overview.md](../docs/arquitectura/overview.md) para diagramas Mermaid de la infraestructura.

## Comandos Útiles

```bash
# Inicializar workspace DEV
cd terraform/environments/dev
terraform init

# Planificar cambios
terraform plan -var-file=dev.tfvars

# Aplicar cambios
terraform apply -auto-approve -var-file=dev.tfvars

# Destruir recursos
terraform destroy -var-file=dev.tfvars
```

## Costos de Infraestructura

| Recurso | Costo estimado |
|---------|---------------|
| Docker Desktop (local) | $0 (gratuito) |
| Terraform Cloud | $0 (free tier) |
| Namespaces K8s | $0 (incluido en Docker Desktop) |
| **Total** | **$0/mes (entorno local)** |

Para despliegue cloud (Kubernetes en AWS/GCP/Azure), los costos incluirían VMs, balanceadores y almacenamiento.
