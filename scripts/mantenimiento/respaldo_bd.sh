#!/usr/bin/env bash
# Respaldo de la base boulder_db en formato custom de pg_dump (comprimido,
# restaurable por tablas con pg_restore). Conserva los últimos RETENCION_DIAS.
#
# Uso:  scripts/mantenimiento/respaldo_bd.sh [carpeta_destino]
# Cron: 0 2 * * * /opt/boulder/scripts/mantenimiento/respaldo_bd.sh /var/backups/boulder
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/../.." && pwd)"
DESTINO="${1:-${BOULDER_RESPALDOS:-$RAIZ/respaldos}}"
RETENCION_DIAS="${RETENCION_DIAS:-14}"
BD="${PGDATABASE:-boulder_db}"
USUARIO="${PGUSER:-postgres}"
SERVIDOR="${PGHOST:-localhost}"
PUERTO="${PGPORT:-5432}"
PG_DUMP="${PG_DUMP:-$(command -v pg_dump || echo "/c/Program Files/PostgreSQL/18/bin/pg_dump.exe")}"

# La contraseña sale de PGPASSWORD o del archivo local ignorado por git.
export PGPASSWORD="${PGPASSWORD:-$(grep '^DB_PASSWORD=' "$RAIZ/src/main/resources/application-local.properties" 2>/dev/null | cut -d= -f2- | tr -d '\r')}"

mkdir -p "$DESTINO"
MARCA="$(date +%Y%m%d_%H%M%S)"
ARCHIVO="$DESTINO/${BD}_${MARCA}.dump"
BITACORA="$DESTINO/respaldos.log"

if "$PG_DUMP" -h "$SERVIDOR" -p "$PUERTO" -U "$USUARIO" -d "$BD" -Fc -Z 6 -f "$ARCHIVO"; then
    TAM="$(du -h "$ARCHIVO" | cut -f1)"
    echo "$(date '+%F %T') OK    $ARCHIVO ($TAM)" >> "$BITACORA"
else
    rm -f "$ARCHIVO"
    echo "$(date '+%F %T') ERROR respaldo de $BD fallido" >> "$BITACORA"
    exit 1
fi

# Retención: borra respaldos más antiguos que RETENCION_DIAS.
find "$DESTINO" -maxdepth 1 -name "${BD}_*.dump" -mtime +"$RETENCION_DIAS" -print -delete \
    | sed "s|^|$(date '+%F %T') BORRADO |" >> "$BITACORA"

echo "$ARCHIVO"
