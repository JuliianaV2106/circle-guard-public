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
      name = "circle-guard-master"
    }
  }
}

provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "docker-desktop"
}

module "namespace_master" {
  source         = "../../modules/namespace"
  namespace_name = var.namespace_name
  environment    = var.environment
}

module "configmap_master" {
  source         = "../../modules/configmap"
  configmap_name = "circle-guard-config"
  namespace_name = module.namespace_master.namespace_name
  environment    = var.environment
  configmap_data = {
    SPRING_PROFILES_ACTIVE     = "prod"
    POSTGRES_HOST              = "host.docker.internal"
    POSTGRES_PORT              = "5432"
    REDIS_HOST                 = "host.docker.internal"
    REDIS_PORT                 = "6379"
    KAFKA_BOOTSTRAP_SERVERS    = "host.docker.internal:9092"
    NEO4J_URI                  = "bolt://host.docker.internal:7687"
    LDAP_URL                   = "ldap://host.docker.internal:389"
    LOG_LEVEL                  = "WARN"
    APP_VERSION                = var.app_version
    MANAGEMENT_ZIPKIN_TRACING_ENDPOINT = "http://jaeger:9411/api/v2/spans"
  }
}

module "gateway_service" {
  source                  = "../../modules/microservice"
  service_name            = "gateway-service"
  namespace_name          = module.namespace_master.namespace_name
  container_port          = 8080
  service_type            = "NodePort"
  node_port               = 31451
  spring_profile          = "prod"
  enable_liveness_probe   = true
  enable_readiness_probe  = true
}

module "notification_service" {
  source                  = "../../modules/microservice"
  service_name            = "notification-service"
  namespace_name          = module.namespace_master.namespace_name
  container_port          = 8084
  spring_profile          = "prod"
  enable_liveness_probe   = true
  enable_readiness_probe  = true
  liveness_initial_delay  = 90
  readiness_initial_delay = 45
}
