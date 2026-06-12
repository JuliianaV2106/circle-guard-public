variable "namespace_name" {
  description = "Namespace de Kubernetes"
  type        = string
}

variable "secrets" {
  description = "Mapa de secretos (key = nombre, value = valor en texto plano)"
  type        = map(string)
  sensitive   = true
}

resource "kubernetes_secret" "this" {
  metadata {
    name      = "circleguard-secrets"
    namespace = var.namespace_name
    labels = {
      project     = "circleguard"
      managed_by  = "terraform"
    }
  }

  data = var.secrets

  type = "Opaque"
}

output "secret_name" {
  value = kubernetes_secret.this.metadata[0].name
}
