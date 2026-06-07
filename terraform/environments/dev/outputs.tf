output "namespace_name" {
  description = "Namespace DEV creado"
  value       = module.namespace_dev.namespace_name
}

output "configmap_name" {
  description = "ConfigMap DEV creado"
  value       = module.configmap_dev.configmap_name
}