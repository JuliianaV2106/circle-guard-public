variable "namespace_name" {
  description = "Nombre del namespace MASTER"
  type        = string
  default     = "circleguard-master"
}

variable "environment" {
  description = "Nombre del ambiente"
  type        = string
  default     = "master"
}

variable "app_version" {
  description = "Version de la aplicacion"
  type        = string
  default     = "1.0.0"
}