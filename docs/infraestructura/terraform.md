# Infraestructura como Codigo — Terraform

## Descripcion

La infraestructura de Circle Guard esta gestionada completamente con Terraform,
siguiendo el principio de Infrastructure as Code. Esto garantiza reproducibilidad,
trazabilidad y consistencia entre ambientes.

## Version

- Terraform: 1.14.6
- Provider kubernetes: 2.38.0

## Estructura modular

El codigo de Terraform sigue una estructura modular con dos capas: modulos
reutilizables y configuraciones por ambiente.

La carpeta `terraform/modules/namespace` contiene el modulo para crear namespaces
de Kubernetes con sus labels estandarizados. La carpeta `terraform/modules/configmap`
contiene el modulo para crear ConfigMaps con la configuracion de cada ambiente.

La carpeta `terraform/environments` tiene tres subcarpetas: `dev`, `stage` y `master`,
cada una con su propio `main.tf`, `variables.tf` y `outputs.tf` que referencian
los modulos compartidos con valores especificos por ambiente.

## Recursos gestionados

### Por ambiente

| Recurso | DEV | STAGE | MASTER |
|---------|-----|-------|--------|
| Namespace K8s | circleguard | circleguard-stage | circleguard-master |
| ConfigMap | circle-guard-config | circle-guard-config | circle-guard-config |
| Labels | environment=dev | environment=stage | environment=master |

### ConfigMap por ambiente

| Variable | DEV | STAGE | MASTER |
|---------|-----|-------|--------|
| SPRING_PROFILES_ACTIVE | dev | stage | prod |
| LOG_LEVEL | DEBUG | INFO | WARN |
| APP_VERSION | 1.0.0 | 1.0.0 | 1.0.0 |
| POSTGRES_HOST | host.docker.internal | host.docker.internal | host.docker.internal |
| REDIS_HOST | host.docker.internal | host.docker.internal | host.docker.internal |
| KAFKA_BOOTSTRAP_SERVERS | host.docker.internal:9092 | host.docker.internal:9092 | host.docker.internal:9092 |

## Comandos de operacion

Inicializar un ambiente:

```bash
cd terraform/environments/<ambiente>
terraform init
```

Ver cambios antes de aplicar:

```bash
terraform plan
```

Aplicar cambios:

```bash
terraform apply -auto-approve
```

Importar recurso existente:

```bash
terraform import module.namespace_dev.kubernetes_namespace.this circleguard
```

Ver estado actual:

```bash
terraform show
```

Destruir infraestructura:

```bash
terraform destroy -auto-approve
```

## Backend remoto — Terraform Cloud

El estado de Terraform se almacena en **HCP Terraform Cloud** de forma remota,
garantizando que el estado sea compartido, versionado y no viva en el repositorio.

**Organizacion:** circle-guard-juliana  
**URL:** https://app.terraform.io/app/circle-guard-juliana

### Workspaces configurados

| Workspace | Ambiente | Modo de ejecucion |
|-----------|---------|-------------------|
| circle-guard-dev | DEV | Local |
| circle-guard-stage | STAGE | Local |
| circle-guard-master | MASTER | Local |

El modo de ejecucion **Local** permite que Terraform acceda a los modulos
locales con rutas relativas, mientras el estado se almacena remotamente
en Terraform Cloud.

### Configuracion del backend en cada ambiente

```hcl
cloud {
  organization = "circle-guard-juliana"
  workspaces {
    name = "circle-guard-dev"
  }
}
```

### Autenticacion

```bash
terraform login
```

Este comando abre el navegador para generar un token de acceso que se
almacena localmente en `~/.terraform.d/credentials.tfrc.json`.

## Decisiones de diseno

La estructura modular permite reutilizar los modulos de namespace y configmap
en los tres ambientes sin duplicar codigo. Cada ambiente solo define sus
variables especificas como el nombre del namespace, el perfil de Spring y
el nivel de log.

Los labels `managed_by=terraform`, `environment` y `project` aplicados a todos
los recursos permiten identificar facilmente que recursos son gestionados por
Terraform y a que ambiente pertenecen.

