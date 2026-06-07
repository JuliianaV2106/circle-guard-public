output "namespace_name" {
  description = "Namespace MASTER creado"
  value       = module.namespace_master.namespace_name
}

output "configmap_name" {
  description = "ConfigMap MASTER creado"
  value       = module.configmap_master.configmap_name
}