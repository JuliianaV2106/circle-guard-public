output "deployment_name" {
  description = "Nombre del deployment"
  value       = kubernetes_deployment.this.metadata[0].name
}

output "service_name" {
  description = "Nombre del service"
  value       = kubernetes_service.this.metadata[0].name
}

output "service_port" {
  description = "Puerto del service"
  value       = kubernetes_service.this.spec[0].port[0].port
}
