output "namespace_name" {
  description = "Namespace STAGE creado"
  value       = module.namespace_stage.namespace_name
}

output "configmap_name" {
  description = "ConfigMap STAGE creado"
  value       = module.configmap_stage.configmap_name
}