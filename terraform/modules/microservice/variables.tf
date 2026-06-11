variable "service_name" {
  description = "Nombre del microservicio"
  type        = string
}

variable "namespace_name" {
  description = "Namespace de Kubernetes"
  type        = string
}

variable "container_port" {
  description = "Puerto del contenedor"
  type        = number
}

variable "service_type" {
  description = "Tipo de Service (ClusterIP o NodePort)"
  type        = string
  default     = "ClusterIP"

  validation {
    condition     = contains(["ClusterIP", "NodePort"], var.service_type)
    error_message = "service_type debe ser ClusterIP o NodePort"
  }
}

variable "node_port" {
  description = "Puerto del NodePort (solo si service_type = NodePort)"
  type        = number
  default     = null
}

variable "replicas" {
  description = "Numero de replicas"
  type        = number
  default     = 1
}

variable "image_name" {
  description = "Nombre de la imagen Docker"
  type        = string
  default     = null
}

variable "image_pull_policy" {
  description = "Politica de pull de la imagen"
  type        = string
  default     = "Never"
}

variable "spring_profile" {
  description = "Perfil de Spring (dev, stage, prod)"
  type        = string
}

variable "enable_liveness_probe" {
  description = "Habilitar liveness probe"
  type        = bool
  default     = false
}

variable "enable_readiness_probe" {
  description = "Habilitar readiness probe"
  type        = bool
  default     = false
}

variable "probe_path" {
  description = "Path del health endpoint"
  type        = string
  default     = "/actuator/health"
}

variable "liveness_initial_delay" {
  description = "Initial delay para liveness probe (segundos)"
  type        = number
  default     = 60
}

variable "readiness_initial_delay" {
  description = "Initial delay para readiness probe (segundos)"
  type        = number
  default     = 30
}

variable "additional_env_vars" {
  description = "Variables de entorno adicionales"
  type        = map(string)
  default     = {}
}
