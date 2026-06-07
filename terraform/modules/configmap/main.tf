resource "kubernetes_config_map" "this" {
  metadata {
    name      = var.configmap_name
    namespace = var.namespace_name

    labels = {
      environment = var.environment
      managed_by  = "terraform"
      project     = "circle-guard"
    }
  }

  data = var.configmap_data
}