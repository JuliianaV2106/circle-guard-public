variable "namespace_name" {
  description = "Nombre del namespace DEV"
  type        = string
  default     = "circleguard"
}

variable "environment" {
  description = "Nombre del ambiente"
  type        = string
  default     = "dev"
}

variable "app_version" {
  description = "Version de la aplicacion"
  type        = string
  default     = "1.0.0"
}