resource "kubernetes_namespace" "this" {
  metadata {
    name = var.namespace_name

    labels = {
      environment = var.environment
      managed_by  = "terraform"
      project     = "circle-guard"
    }
  }
}