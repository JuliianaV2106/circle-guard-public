# Seguridad — Circle Guard

## 1. Escaneo Continuo de Vulnerabilidades

### 1.1 Stack de Seguridad en CI/CD

| Herramienta | Tipo | Objetivo | Pipeline |
|------------|------|----------|----------|
| **SonarQube** | SAST | Análisis estático de código | DEV, STAGE, MASTER |
| **OWASP Dependency-Check** | SCA | Vulnerabilidades en dependencias | DEV, MASTER |
| **Trivy** | Container Scan | Vulnerabilidades en imágenes Docker | DEV, STAGE, MASTER |
| **OWASP ZAP** | DAST | Pruebas de penetración dinámicas | DEV, STAGE, MASTER |

### 1.2 Etapas en Pipeline

```
Build → Unit Tests → Dependency Check → SonarQube → Trivy → ZAP
```

- **Dependency Check**: Analiza todas las dependencias Gradle contra la NVD
- **SonarQube**: Quality gates, code smells, security hotspots
- **Trivy**: Escanea imágenes Docker por CVEs (HIGH/CRITICAL)
- **OWASP ZAP**: Baseline scan contra gateway-service

### 1.3 Políticas de Severidad

| Pipeline | Trivy Severity | ZAP | Dependency Check |
|----------|---------------|-----|-----------------|
| DEV | HIGH, CRITICAL | Baseline | Report only |
| STAGE | CRITICAL | Baseline | N/A |
| MASTER | HIGH, CRITICAL | Baseline | Report only |

## 2. Gestión Segura de Secretos

### 2.1 Problema Identificado

Actualmente los secretos están hardcodeados en `application.yml`:
- `jwt.secret`, `qr.secret`, `spring.datasource.password`, `spring.ldap.password`
- `vault.secret`, `vault.salt`, `vault.hash-salt`

### 2.2 Solución Implementada

```
application.yml (valores por defecto DEV)
       ↓
  Kubernetes Secret (circleguard-secrets)
       ↓
  env_from → Deployment (sobrescribe por env vars)
       ↓
  Spring Boot (environment properties > YAML)
```

### 2.3 Terraform Secrets Module

```hcl
module "secrets_dev" {
  source         = "../../modules/secrets"
  namespace_name = "circleguard"
  secrets = {
    JWT_SECRET  = var.jwt_secret
    QR_SECRET   = var.qr_secret
    SPRING_DATASOURCE_PASSWORD = var.db_password
    # ...
  }
}
```

**IMPORTANTE**: No commitear valores de secretos. Usar Terraform Cloud sensitive variables o `scripts/init-secrets.sh`.

### 2.4 Bootstrap Manual

```bash
# Desde archivo env
./scripts/init-secrets.sh circleguard .env.production

# Con valores por defecto (solo DEV local)
./scripts/init-secrets.sh circleguard
```

## 3. RBAC para Acceso a Recursos

### 3.1 Kubernetes RBAC

Cada microservicio tiene su propio ServiceAccount con permisos mínimos:

| Servicio | ServiceAccount | Role | Permisos |
|----------|---------------|------|----------|
| auth-service | `auth-service-sa` | `auth-service-pod-reader` | get/list/watch pods, services, endpoints |
| gateway-service | `gateway-service-sa` | `gateway-service-pod-reader` | get/list/watch pods, services, endpoints |
| identity-service | `identity-service-sa` | `identity-service-pod-reader` | get/list/watch pods, services, endpoints |
| form-service | `form-service-sa` | `form-service-pod-reader` | get/list/watch pods, services, endpoints |
| notification-service | `notification-service-sa` | `notification-service-pod-reader` | get/list/watch pods, services, endpoints |
| dashboard-service | `dashboard-service-sa` | `dashboard-service-pod-reader` | get/list/watch pods, services, endpoints |

### 3.2 Terraform RBAC Module

Creado en `terraform/modules/rbac/`:
- `kubernetes_service_account` por servicio
- `kubernetes_role` con permisos de solo lectura
- `kubernetes_role_binding` vinculando SA al Role

### 3.3 Microservicio con ServiceAccount

```hcl
module "auth_service" {
  source               = "../../modules/microservice"
  service_account_name = "auth-service-sa"
  # ...
}
```

## 4. TLS para Servicios Expuestos

### 4.1 Arquitectura TLS

```
Cliente → HTTPS :443 → Ingress (nginx) → HTTP :8080 → Gateway Service
                              ↑
                     TLS termination en Ingress
                     Certificado: circleguard-tls (K8s Secret)
```

### 4.2 Generación de Certificados

```bash
./scripts/gen-certs.sh api.circleguard.edu certs/
```

Esto genera:
- `ca.crt` / `ca.key` — CA autofirmada
- `tls.crt` / `tls.key` — Certificado del servidor
- `keystore.p12` — Keystore PKCS12 para Spring Boot (password: changeit)

### 4.3 Instalación en Kubernetes

```bash
kubectl create secret tls circleguard-tls \
  --namespace circleguard-master \
  --key certs/tls.key \
  --cert certs/tls.crt
```

### 4.4 Ingress con TLS

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: gateway-ingress
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
    - hosts: [api.circleguard.edu]
      secretName: circleguard-tls
  rules:
    - host: api.circleguard.edu
      http:
        paths:
          - path: /
            backend:
              service:
                name: gateway-service
                port: 8080
```

### 4.5 Producción (Let's Encrypt + cert-manager)

Para producción, usar cert-manager con Let's Encrypt:

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/latest/download/cert-manager.yaml
```

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@circleguard.edu
    privateKeySecretRef:
      name: letsencrypt-prod-key
    solvers:
      - http01:
          ingress:
            class: nginx
```

## 5. Resumen de Mejoras Implementadas

| Medida | Antes | Después |
|--------|-------|---------|
| SCA (Dependency Check) | ❌ No existía | ✅ Agregado a build.gradle.kts + Jenkinsfiles |
| Gestión de secretos | Hardcodeados en YAML | ✅ Kubernetes Secrets + Terraform module |
| RBAC en K8s | No existía | ✅ ServiceAccount + Role + RoleBinding por servicio |
| TLS | HTTP plano | ✅ Ingress con TLS + certificados autofirmados |
| Escaneo Trivy | Solo HIGH/CRITICAL | ✅ Mantenido + documentado |
| Escaneo ZAP | Solo gateway | ✅ Mantenido + documentado |

---

*Documento controlado — Versión 1.0 — Última actualización: 2026-06-11*
