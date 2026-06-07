variable "namespace_name" {
  description = "Nombre del namespace STAGE"
  type        = string
  default     = "circleguard-stage"
}

variable "environment" {
  description = "Nombre del ambiente"
  type        = string
  default     = "stage"
}

variable "app_version" {
  description = "Version de la aplicacion"
  type        = string
  default     = "1.0.0"
}