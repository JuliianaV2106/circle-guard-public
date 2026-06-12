#!/bin/bash
# Generate self-signed TLS certificates for CircleGuard
# Usage: ./scripts/gen-certs.sh [domain] [output-dir]

set -euo pipefail

DOMAIN="${1:-circleguard.local}"
OUTPUT="${2:-certs}"

mkdir -p "$OUTPUT"

echo "Generating CA key and certificate..."
openssl req -x509 -sha256 -days 3650 -nodes \
  -newkey rsa:4096 \
  -keyout "$OUTPUT/ca.key" \
  -out "$OUTPUT/ca.crt" \
  -subj "/CN=CircleGuard Dev CA/O=CircleGuard/C=CO"

echo "Generating server key..."
openssl req -new -newkey rsa:4096 -nodes \
  -keyout "$OUTPUT/tls.key" \
  -out "$OUTPUT/tls.csr" \
  -subj "/CN=$DOMAIN/O=CircleGuard/C=CO" \
  -addext "subjectAltName=DNS:$DOMAIN,DNS:localhost,IP:127.0.0.1"

echo "Signing server certificate..."
openssl x509 -req -sha256 -days 365 \
  -in "$OUTPUT/tls.csr" \
  -CA "$OUTPUT/ca.crt" \
  -CAkey "$OUTPUT/ca.key" \
  -CAcreateserial \
  -out "$OUTPUT/tls.crt" \
  -extfile <(echo "subjectAltName=DNS:$DOMAIN,DNS:localhost,IP:127.0.0.1")

echo "Generating PKCS12 keystore for Spring Boot..."
openssl pkcs12 -export \
  -in "$OUTPUT/tls.crt" \
  -inkey "$OUTPUT/tls.key" \
  -out "$OUTPUT/keystore.p12" \
  -name circleguard \
  -password pass:changeit

echo "Certificates generated in: $OUTPUT/"
echo ""
echo "Files:"
ls -la "$OUTPUT/"
echo ""
echo "To create K8s TLS secret:"
echo "  kubectl create secret tls circleguard-tls \\"
echo "    --namespace <namespace> \\"
echo "    --key $OUTPUT/tls.key \\"
echo "    --cert $OUTPUT/tls.crt"
echo ""
echo "To trust the CA in your system:"
echo "  # macOS: sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain $OUTPUT/ca.crt"
echo "  # Linux: sudo cp $OUTPUT/ca.crt /usr/local/share/ca-certificates/ && sudo update-ca-certificates"
echo "  # Windows: certutil -addstore Root $OUTPUT/ca.crt"
