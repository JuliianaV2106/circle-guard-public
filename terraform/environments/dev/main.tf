terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
  }

  cloud {
    organization = "circle-guard-juliana"
    workspaces {
      name = "circle-guard-dev"
    }
  }
}

provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "docker-desktop"
}

module "namespace_dev" {
  source         = "../../modules/namespace"
  namespace_name = var.namespace_name
  environment    = var.environment
}

module "configmap_dev" {
  source         = "../../modules/configmap"
  configmap_name = "circle-guard-config"
  namespace_name = module.namespace_dev.namespace_name
  environment    = var.environment
  configmap_data = {
    SPRING_PROFILES_ACTIVE     = "dev"
    POSTGRES_HOST              = "host.docker.internal"
    POSTGRES_PORT              = "5432"
    REDIS_HOST                 = "host.docker.internal"
    REDIS_PORT                 = "6379"
    KAFKA_BOOTSTRAP_SERVERS    = "host.docker.internal:9092"
    NEO4J_URI                  = "bolt://host.docker.internal:7687"
    LDAP_URL                   = "ldap://host.docker.internal:389"
    LOG_LEVEL                  = "DEBUG"
    APP_VERSION                = var.app_version
  }
}

module "auth_service" {
  source         = "../../modules/microservice"
  service_name   = "auth-service"
  namespace_name = module.namespace_dev.namespace_name
  container_port = 8081
  spring_profile = var.environment
}

module "gateway_service" {
  source                  = "../../modules/microservice"
  service_name            = "gateway-service"
  namespace_name          = module.namespace_dev.namespace_name
  container_port          = 8080
  service_type            = "NodePort"
  node_port               = 31449
  spring_profile          = var.environment
  enable_liveness_probe   = true
  enable_readiness_probe  = true
  probe_path              = "/actuator/health"
}

module "identity_service" {
  source         = "../../modules/microservice"
  service_name   = "identity-service"
  namespace_name = module.namespace_dev.namespace_name
  container_port = 8082
  spring_profile = var.environment
}

module "form_service" {
  source         = "../../modules/microservice"
  service_name   = "form-service"
  namespace_name = module.namespace_dev.namespace_name
  container_port = 8083
  spring_profile = var.environment
}

module "notification_service" {
  source         = "../../modules/microservice"
  service_name   = "notification-service"
  namespace_name = module.namespace_dev.namespace_name
  container_port = 8084
  spring_profile = var.environment
}

module "dashboard_service" {
  source         = "../../modules/microservice"
  service_name   = "dashboard-service"
  namespace_name = module.namespace_dev.namespace_name
  container_port = 8085
  spring_profile = var.environment
}
