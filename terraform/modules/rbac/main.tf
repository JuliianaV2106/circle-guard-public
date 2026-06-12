variable "namespace_name" {
  description = "Namespace de Kubernetes"
  type        = string
}

variable "service_names" {
  description = "Lista de nombres de servicios para crear ServiceAccount"
  type        = list(string)
}

resource "kubernetes_service_account" "this" {
  for_each = toset(var.service_names)

  metadata {
    name      = "${each.value}-sa"
    namespace = var.namespace_name
    labels = {
      app         = each.value
      project     = "circleguard"
      managed_by  = "terraform"
    }
  }

  automount_service_account_token = true
}

resource "kubernetes_role" "pod_reader" {
  for_each = toset(var.service_names)

  metadata {
    name      = "${each.value}-pod-reader"
    namespace = var.namespace_name
    labels = {
      app        = each.value
      project    = "circleguard"
      managed_by = "terraform"
    }
  }

  rule {
    api_groups = [""]
    resources  = ["pods", "pods/log", "services", "endpoints"]
    verbs      = ["get", "list", "watch"]
  }
}

resource "kubernetes_role_binding" "this" {
  for_each = toset(var.service_names)

  metadata {
    name      = "${each.value}-rb"
    namespace = var.namespace_name
    labels = {
      app        = each.value
      project    = "circleguard"
      managed_by = "terraform"
    }
  }

  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "Role"
    name      = kubernetes_role.pod_reader[each.value].metadata[0].name
  }

  subject {
    kind      = "ServiceAccount"
    name      = kubernetes_service_account.this[each.value].metadata[0].name
    namespace = var.namespace_name
  }
}

output "service_accounts" {
  value = {
    for sa in kubernetes_service_account.this : sa.metadata[0].name => sa.metadata[0].name
  }
}
