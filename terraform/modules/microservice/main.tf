locals {
  image = var.image_name != null ? var.image_name : "circleguard/${var.service_name}:latest"
}

resource "kubernetes_deployment" "this" {
  metadata {
    name      = var.service_name
    namespace = var.namespace_name
    labels = {
      app         = var.service_name
      environment = var.spring_profile
      managed_by  = "terraform"
    }
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = {
        app = var.service_name
      }
    }

    template {
      metadata {
        labels = {
          app         = var.service_name
          environment = var.spring_profile
        }
      }

      spec {
        container {
          name              = var.service_name
          image             = local.image
          image_pull_policy = var.image_pull_policy

          port {
            container_port = var.container_port
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = var.spring_profile
          }

          dynamic "env" {
            for_each = var.additional_env_vars
            content {
              name  = env.key
              value = env.value
            }
          }

          dynamic "liveness_probe" {
            for_each = var.enable_liveness_probe ? [1] : []
            content {
              http_get {
                path = var.probe_path
                port = var.container_port
              }
              initial_delay_seconds = var.liveness_initial_delay
              period_seconds        = 15
              failure_threshold     = 3
            }
          }

          dynamic "readiness_probe" {
            for_each = var.enable_readiness_probe ? [1] : []
            content {
              http_get {
                path = var.probe_path
                port = var.container_port
              }
              initial_delay_seconds = var.readiness_initial_delay
              period_seconds        = 10
              failure_threshold     = 3
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "this" {
  metadata {
    name      = var.service_name
    namespace = var.namespace_name
    labels = {
      app         = var.service_name
      environment = var.spring_profile
      managed_by  = "terraform"
    }
  }

  spec {
    selector = {
      app = var.service_name
    }

    port {
      port        = var.container_port
      target_port = var.container_port
      node_port   = var.service_type == "NodePort" ? var.node_port : null
    }

    type = var.service_type
  }
}
