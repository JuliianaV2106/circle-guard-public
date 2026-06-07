variable "configmap_name" {
  description = "Nombre del ConfigMap"
  type        = string
}

variable "namespace_name" {
  description = "Namespace donde se crea el ConfigMap"
  type        = string
}

variable "environment" {
  description = "Ambiente: dev, stage o master"
  type        = string
}

variable "configmap_data" {
  description = "Datos del ConfigMap como mapa de strings"
  type        = map(string)
}