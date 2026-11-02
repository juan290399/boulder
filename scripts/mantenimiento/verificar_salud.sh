#!/usr/bin/env bash
# Consulta /actuator/health y deja constancia en la bitácora de monitoreo. Si
# la aplicación no responde UP, sale con código 1 para que cron o el
# programador de tareas lo registren como fallo.
#
# Uso:  scripts/mantenimiento/verificar_salud.sh [url_base]
# Cron: */5 * * * * /opt/boulder/scripts/mantenimiento/verificar_salud.sh https://boulder.ejemplo.pe
set -uo pipefail

URL="${1:-${BOULDER_URL:-http://localhost:8080}}/actuator/health"
RAIZ="$(cd "$(dirname "$0")/../.." && pwd)"
BITACORA="${BOULDER_MONITOREO_LOG:-$RAIZ/logs/monitoreo.log}"
mkdir -p "$(dirname "$BITACORA")"

INICIO=$(date +%s%3N)
RESPUESTA="$(curl -s -m 10 -w ' %{http_code}' "$URL")"
CODIGO="${RESPUESTA##* }"
CUERPO="${RESPUESTA% *}"
MS=$(( $(date +%s%3N) - INICIO ))

# Uso de disco del volumen donde vive la aplicación, como salud del sistema.
DISCO="$(df -P "$RAIZ" | awk 'NR==2 {print $5}')"

if [ "$CODIGO" = "200" ] && echo "$CUERPO" | grep -q '"status":"UP"'; then
    echo "$(date '+%F %T') UP    ${MS} ms disco=${DISCO}" >> "$BITACORA"
else
    echo "$(date '+%F %T') CAIDA codigo=${CODIGO:-sin respuesta} ${MS} ms disco=${DISCO}" >> "$BITACORA"
    exit 1
fi
