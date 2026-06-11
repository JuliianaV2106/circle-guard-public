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
      name = "circle-guard-stage"
    }
  }
}

provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "docker-desktop"
}

module "namespace_stage" {
  source         = "../../modules/namespace"
  namespace_name = var.namespace_name
  environment    = var.environment
}

module "configmap_stage" {
  source         = "../../modules/configmap"
  configmap_name = "circle-guard-config"
  namespace_name = module.namespace_stage.namespace_name
  environment    = var.environment
  configmap_data = {
    SPRING_PROFILES_ACTIVE     = "stage"
    POSTGRES_HOST              = "host.docker.internal"
    POSTGRES_PORT              = "5432"
    REDIS_HOST                 = "host.docker.internal"
    REDIS_PORT                 = "6379"
    KAFKA_BOOTSTRAP_SERVERS    = "host.docker.internal:9092"
    NEO4J_URI                  = "bolt://host.docker.internal:7687"
    LDAP_URL                   = "ldap://host.docker.internal:389"
    LOG_LEVEL                  = "INFO"
    APP_VERSION                = var.app_version
  }
}

module "gateway_service" {
  source                  = "../../modules/microservice"
  service_name            = "gateway-service"
  namespace_name          = module.namespace_stage.namespace_name
  container_port          = 8080
  service_type            = "NodePort"
  node_port               = 31450
  spring_profile          = "stage"
  enable_liveness_probe   = true
  enable_readiness_probe  = true
}

module "notification_service" {
  source         = "../../modules/microservice"
  service_name   = "notification-service"
  namespace_name = module.namespace_stage.namespace_name
  container_port = 8084
  spring_profile = "stage"
}
