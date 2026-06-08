# Arquitectura de Infraestructura — Circle Guard

## Vision general

Circle Guard es un sistema de microservicios desplegado en Kubernetes con
tres ambientes separados gestionados por Terraform.

## Diagrama de infraestructura

```mermaid
graph TB
    subgraph TerraformCloud["Terraform Cloud — Estado Remoto"]
        WS1[circle-guard-dev]
        WS2[circle-guard-stage]
        WS3[circle-guard-master]
    end

    subgraph Kubernetes["Kubernetes — Docker Desktop"]
        subgraph DEV["Namespace: circleguard"]
            CM1[ConfigMap: circle-guard-config\nSPRING_PROFILES=dev\nLOG_LEVEL=DEBUG]
            P1[gateway-service]
            P2[notification-service]
            P3[auth-service]
            P4[identity-service]
            P5[form-service]
            P6[dashboard-service]
        end

        subgraph STAGE["Namespace: circleguard-stage"]
            CM2[ConfigMap: circle-guard-config\nSPRING_PROFILES=stage\nLOG_LEVEL=INFO]
            P7[gateway-service\nNodePort 31450]
            P8[notification-service]
        end

        subgraph MASTER["Namespace: circleguard-master"]
            CM3[ConfigMap: circle-guard-config\nSPRING_PROFILES=prod\nLOG_LEVEL=WARN]
            P9[gateway-service\nNodePort 31451]
            P10[notification-service]
        end
    end

    subgraph Infraestructura["Infraestructura Docker"]
        DB1[(PostgreSQL 16\npuerto 5432)]
        DB2[(Neo4j 5.26\npuerto 7687)]
        DB3[(Redis 7.2\npuerto 6379)]
        MQ[Kafka 7.6\npuerto 9092]
        LDAP[OpenLDAP\npuerto 389]
    end

    subgraph CI["CI/CD — Jenkins"]
        J1[Pipeline DEV\nJenkinsfile]
        J2[Pipeline STAGE\nJenkinsfile.stage]
        J3[Pipeline MASTER\nJenkinsfile.master]
    end

    WS1 -->|gestiona estado| DEV
    WS2 -->|gestiona estado| STAGE
    WS3 -->|gestiona estado| MASTER

    J1 -->|despliega| DEV
    J2 -->|despliega| STAGE
    J3 -->|despliega| MASTER

    DEV -->|conecta via host.docker.internal| Infraestructura
    STAGE -->|conecta via host.docker.internal| Infraestructura
    MASTER -->|conecta via host.docker.internal| Infraestructura
```

## Diagrama de comunicacion entre microservicios

```mermaid
sequenceDiagram
    participant U as Usuario
    participant G as gateway-service
    participant A as auth-service
    participant I as identity-service
    participant R as Redis
    participant DB as PostgreSQL

    U->>A: POST /api/v1/auth/login
    A->>DB: valida credenciales
    A->>I: GET anonymousId(username)
    I->>DB: consulta mapeo identidad
    I-->>A: anonymousId (UUID)
    A-->>U: JWT token con anonymousId

    U->>G: POST /api/v1/gate/validate (JWT)
    G->>G: valida firma JWT HMAC-SHA256
    G->>R: GET user:status:{anonymousId}
    R-->>G: GREEN / YELLOW / RED
    G-->>U: resultado de acceso
```

## Diagrama de pipeline CI/CD

```mermaid
graph LR
    subgraph DEV["Pipeline DEV"]
        D1[Checkout] --> D2[Build]
        D2 --> D3[Tests 23]
        D3 --> D4[Docker Build]
        D4 --> D5[SonarQube]
        D5 --> D6[Trivy]
        D6 --> D7[Deploy K8s DEV]
    end

    subgraph STAGE["Pipeline STAGE"]
        S1[Checkout] --> S2[Verify Images]
        S2 --> S3[Deploy K8s STAGE]
        S3 --> S4[Health Check]
        S4 --> S5[Smoke Tests]
        S5 --> S6[Trivy CRITICAL]
    end

    subgraph MASTER["Pipeline MASTER"]
        M1[Checkout] --> M2[Build]
        M2 --> M3[Unit Tests]
        M3 --> M4[SonarQube]
        M4 --> M5[Docker Build vX.X.X]
        M5 --> M6[Trivy]
        M6 --> M7[Deploy STAGE]
        M7 --> M8[System Tests]
        M8 --> M9[Deploy MASTER]
        M9 --> M10[Release Notes]
    end

    DEV -->|imagenes aprobadas| STAGE
    STAGE -->|validacion exitosa| MASTER
```

## Modulos Terraform

```mermaid
graph TB
    subgraph Modulos["terraform/modules"]
        NS[namespace\nmain.tf\nvariables.tf\noutputs.tf]
        CM[configmap\nmain.tf\nvariables.tf\noutputs.tf]
    end

    subgraph Ambientes["terraform/environments"]
        DEV[dev\nmain.tf]
        STG[stage\nmain.tf]
        MST[master\nmain.tf]
    end

    DEV -->|usa| NS
    DEV -->|usa| CM
    STG -->|usa| NS
    STG -->|usa| CM
    MST -->|usa| NS
    MST -->|usa| CM
```

## Stack tecnologico

| Capa | Tecnologia | Version | Proposito |
|------|-----------|---------|-----------|
| Orquestacion | Kubernetes | 1.32.2 | Despliegue de microservicios |
| IaC | Terraform | 1.14.6 | Gestion de infraestructura |
| Estado remoto | HCP Terraform Cloud | - | Estado compartido de Terraform |
| CI/CD | Jenkins | LTS | Pipelines automatizados |
| Calidad | SonarQube | LTS Community | Analisis estatico |
| Seguridad | Trivy | 0.70.0 | Escaneo de vulnerabilidades |
| Base de datos | PostgreSQL | 16 | Identidades y formularios |
| Grafo | Neo4j | 5.26 | Trazabilidad de contactos |
| Cache | Redis | 7.2 | Estado sanitario en tiempo real |
| Mensajeria | Apache Kafka | 7.6 | Eventos asincronos |
| Directorio | OpenLDAP | 1.5.0 | Autenticacion universitaria |

---
*Arquitectura documentada como parte del Proyecto Final — Ingenieria de Software V*
