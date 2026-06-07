output "configmap_name" {
  description = "Nombre del ConfigMap creado"
  value       = kubernetes_config_map.this.metadata[0].name
}