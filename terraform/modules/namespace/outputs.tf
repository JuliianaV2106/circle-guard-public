output "namespace_name" {
  description = "Nombre del namespace creado"
  value       = kubernetes_namespace.this.metadata[0].name
}

output "namespace_id" {
  description = "ID del namespace creado"
  value       = kubernetes_namespace.this.id
}