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

variable "jwt_secret" {
  description = "Secreto JWT para firmar tokens"
  type        = string
  sensitive   = true
}

variable "qr_secret" {
  description = "Secreto QR para firmar codigos QR"
  type        = string
  sensitive   = true
}

variable "db_username" {
  description = "Usuario de PostgreSQL"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Password de PostgreSQL"
  type        = string
  sensitive   = true
}

variable "ldap_password" {
  description = "Password de LDAP admin"
  type        = string
  sensitive   = true
}

variable "vault_secret" {
  description = "Secreto de encriptacion del Vault de identidades"
  type        = string
  sensitive   = true
}

variable "vault_salt" {
  description = "Salt para encriptacion del Vault"
  type        = string
  sensitive   = true
}

variable "vault_hash_salt" {
  description = "Salt para hashing de identidades"
  type        = string
  sensitive   = true
}