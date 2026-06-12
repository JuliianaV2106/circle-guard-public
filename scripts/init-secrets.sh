#!/bin/bash
# Bootstrap Kubernetes Secrets for CircleGuard
# Usage: ./scripts/init-secrets.sh <namespace> [env-file]

set -euo pipefail

NAMESPACE="${1:?Usage: $0 <namespace> [env-file]}"
ENV_FILE="${2:-}"
SECRET_NAME="circleguard-secrets"

if [ -n "$ENV_FILE" ] && [ -f "$ENV_FILE" ]; then
    echo "Creating secrets from: $ENV_FILE"
    kubectl create secret generic "$SECRET_NAME" \
        --namespace "$NAMESPACE" \
        --from-env-file "$ENV_FILE" \
        --dry-run=client -o yaml | kubectl apply -f -
else
    echo "Creating secrets with default values (DEV only)..."
    kubectl create secret generic "$SECRET_NAME" \
        --namespace "$NAMESPACE" \
        --from-literal=JWT_SECRET="my-super-secret-dev-key-32-chars-long-12345678" \
        --from-literal=QR_SECRET="my-qr-secret-key-for-dev-1234567890" \
        --from-literal=SPRING_DATASOURCE_USERNAME="admin" \
        --from-literal=SPRING_DATASOURCE_PASSWORD="password" \
        --from-literal=SPRING_LDAP_PASSWORD="admin" \
        --from-literal=VAULT_SECRET="746573742d7365637265742d33322d63686172732d6c6f6e672d313233343536" \
        --from-literal=VAULT_SALT="deadbeef" \
        --from-literal=VAULT_HASH_SALT="12345678" \
        --from-literal=POSTGRES_PASSWORD="password" \
        --dry-run=client -o yaml | kubectl apply -f -
fi

echo "Secret '$SECRET_NAME' created in namespace '$NAMESPACE'"
