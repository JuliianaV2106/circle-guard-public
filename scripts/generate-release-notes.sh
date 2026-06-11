#!/bin/bash
# Generate Release Notes from git log
# Usage: ./scripts/generate-release-notes.sh <version> [output-file]

set -euo pipefail

VERSION="${1:?Usage: $0 <version> [output-file]}"
OUTPUT="${2:-RELEASE-NOTES-v${VERSION}.md}"
DATE=$(date +%Y-%m-%d)
LAST_TAG=$(git describe --tags --match "v*.*.*" --abbrev=0 2>/dev/null || echo "")

if [ -z "$LAST_TAG" ]; then
    RANGE="HEAD"
    echo "# Release Notes — Circle Guard v${VERSION}" > "$OUTPUT"
    echo "" >> "$OUTPUT"
    echo "**Primera release — no hay tags anteriores**" >> "$OUTPUT"
else
    RANGE="${LAST_TAG}..HEAD"
    echo "# Release Notes — Circle Guard v${VERSION}" > "$OUTPUT"
    echo "" >> "$OUTPUT"
fi

echo "" >> "$OUTPUT"
echo "**Fecha de release:** ${DATE}" >> "$OUTPUT"
echo "**Versión:** v${VERSION}" >> "$OUTPUT"
echo "**Rango de commits:** ${RANGE}" >> "$OUTPUT"
echo "" >> "$OUTPUT"

# Changelog grouped by conventional commit type
echo "## Cambios incluidos" >> "$OUTPUT"
echo "" >> "$OUTPUT"

for type in feat fix docs refactor chore test ci style perf; do
    case "$type" in
        feat)    LABEL="🚀 Nuevas funcionalidades" ;;
        fix)     LABEL="🐛 Correcciones de bugs" ;;
        docs)    LABEL="📚 Documentación" ;;
        refactor) LABEL="🔧 Refactorización" ;;
        chore)   LABEL="🧹 Tareas de mantenimiento" ;;
        test)    LABEL="🧪 Pruebas" ;;
        ci)      LABEL="⚙️ Integración continua" ;;
        style)   LABEL="🎨 Estilo de código" ;;
        perf)    LABEL="⚡ Mejoras de rendimiento" ;;
    esac

    COMMITS=$(git log "$RANGE" --oneline --grep="^${type}:" 2>/dev/null || true)
    if [ -n "$COMMITS" ]; then
        echo "### ${LABEL}" >> "$OUTPUT"
        echo "" >> "$OUTPUT"
        echo '```' >> "$OUTPUT"
        echo "$COMMITS" >> "$OUTPUT"
        echo '```' >> "$OUTPUT"
        echo "" >> "$OUTPUT"
    fi
done

# Commits sem cabeçalho convencional
OTHER=$(git log "$RANGE" --oneline 2>/dev/null | grep -v "^[0-9a-f]\{7,9\} \(feat\|fix\|docs\|refactor\|chore\|test\|ci\|style\|perf\):" || true)
if [ -n "$OTHER" ]; then
    echo "### Otros cambios" >> "$OUTPUT"
    echo "" >> "$OUTPUT"
    echo '```' >> "$OUTPUT"
    echo "$OTHER" >> "$OUTPUT"
    echo '```' >> "$OUTPUT"
    echo "" >> "$OUTPUT"
fi

# Servicios desplegados
echo "## Servicios desplegados" >> "$OUTPUT"
echo "" >> "$OUTPUT"
echo "| Servicio | Imagen |" >> "$OUTPUT"
echo "|---------|--------|" >> "$OUTPUT"
for service in auth-service gateway-service identity-service form-service notification-service dashboard-service; do
    echo "| ${service} | \`circleguard/${service}:${VERSION}\` |" >> "$OUTPUT"
done
echo "" >> "$OUTPUT"

# Rollback
echo "## Plan de Rollback" >> "$OUTPUT"
echo "" >> "$OUTPUT"
echo '```bash' >> "$OUTPUT"
echo "# Revertir a la versión anterior" >> "$OUTPUT"
echo "kubectl rollout undo deployment/gateway-service -n circleguard-master" >> "$OUTPUT"
echo "kubectl rollout undo deployment/notification-service -n circleguard-master" >> "$OUTPUT"
echo "" >> "$OUTPUT"
echo "# O desplegar versión específica" >> "$OUTPUT"
echo "kubectl set image deployment/gateway-service \\" >> "$OUTPUT"
echo "  gateway-service=circleguard/gateway-service:v${VERSION} \\" >> "$OUTPUT"
echo "  -n circleguard-master" >> "$OUTPUT"
echo '```' >> "$OUTPUT"
echo "" >> "$OUTPUT"

# Change management info
echo "## Change Management" >> "$OUTPUT"
echo "" >> "$OUTPUT"
echo "- **CRQ ID:** CRQ-$(git rev-list --count HEAD 2>/dev/null || echo '000')" >> "$OUTPUT"
echo "- **Tipo:** Release de funcionalidad" >> "$OUTPUT"
echo "- **Impacto:** Bajo" >> "$OUTPUT"
echo "- **Rollback:** kubectl rollout undo" >> "$OUTPUT"
echo "- **Aprobado por:** Pipeline CI/CD automático" >> "$OUTPUT"
echo "" >> "$OUTPUT"

echo "---" >> "$OUTPUT"
echo "*Generado automáticamente por scripts/generate-release-notes.sh — ${DATE}*" >> "$OUTPUT"

echo "Release Notes generadas: ${OUTPUT}"
cat "$OUTPUT"
