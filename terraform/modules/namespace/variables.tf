variable "namespace_name" {
  description = "Nombre del namespace de Kubernetes"
  type        = string
}

variable "environment" {
  description = "Ambiente: dev, stage o master"
  type        = string

  validation {
    condition     = contains(["dev", "stage", "master"], var.environment)
    error_message = "El ambiente debe ser dev, stage o master."
  }
}