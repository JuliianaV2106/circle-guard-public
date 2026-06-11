#!/bin/bash
# OWASP ZAP Baseline Scan
# Usage: ./zap-scan.sh <target-url> [output-dir]
set -e

TARGET_URL="${1:-http://host.docker.internal:31449}"
OUTPUT_DIR="${2:-./build/reports/zap}"
REPORT_FILE="${OUTPUT_DIR}/zap-report.html"

mkdir -p "${OUTPUT_DIR}"

echo "=== OWASP ZAP Baseline Scan ==="
echo "Target: ${TARGET_URL}"

docker run --rm \
  -v "${OUTPUT_DIR}:/zap/wrk" \
  ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py \
  -t "${TARGET_URL}" \
  -g gen.conf \
  -r zap-report.html \
  -w zap-report.md \
  -x zap-report.xml \
  -I \
  || true

echo "ZAP scan completed. Report: ${REPORT_FILE}"
