#!/usr/bin/env bash
# Restaura un respaldo de respaldo_bd.sh en una base. Si la base destino no
# existe, la crea; si existe, exige --reemplazar para no pisarla por error.
#
# Uso:  scripts/mantenimiento/restaurar_bd.sh archivo.dump [base_destino] [--reemplazar]
set -euo pipefail

ARCHIVO="${1:?Indica el archivo .dump}"
DESTINO="${2:-boulder_db_restaurada}"
REEMPLAZAR="${3:-}"
RAIZ="$(cd "$(dirname "$0")/../.." && pwd)"
USUARIO="${PGUSER:-postgres}"
SERVIDOR="${PGHOST:-localhost}"
PUERTO="${PGPORT:-5432}"
BIN="${PG_BIN:-$(dirname "$(command -v pg_restore || echo "/c/Program Files/PostgreSQL/18/bin/pg_restore.exe")")}"

export PGPASSWORD="${PGPASSWORD:-$(grep '^DB_PASSWORD=' "$RAIZ/src/main/resources/application-local.properties" 2>/dev/null | cut -d= -f2- | tr -d '\r')}"

[ -f "$ARCHIVO" ] || { echo "No existe $ARCHIVO" >&2; exit 1; }
"$BIN/pg_restore" -l "$ARCHIVO" > /dev/null || { echo "$ARCHIVO no es un respaldo válido" >&2; exit 1; }

EXISTE="$("$BIN/psql" -h "$SERVIDOR" -p "$PUERTO" -U "$USUARIO" -d postgres -Atc \
    "SELECT 1 FROM pg_database WHERE datname = '$DESTINO'")"
if [ "$EXISTE" = "1" ] && [ "$REEMPLAZAR" != "--reemplazar" ]; then
    echo "La base $DESTINO ya existe; agrega --reemplazar para sobrescribirla." >&2
    exit 1
fi
[ "$EXISTE" = "1" ] || "$BIN/createdb" -h "$SERVIDOR" -p "$PUERTO" -U "$USUARIO" "$DESTINO"

"$BIN/pg_restore" -h "$SERVIDOR" -p "$PUERTO" -U "$USUARIO" -d "$DESTINO" --clean --if-exists --no-owner "$ARCHIVO"
echo "Restaurado $ARCHIVO en $DESTINO"
