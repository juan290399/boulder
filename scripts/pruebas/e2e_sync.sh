#!/usr/bin/env bash
# Prueba de punta a punta de la sincronización GeoLog -> Drilling contra el
# servidor de pruebas (8081) y la base real. Deja la base como la encontró.
set -u
export PATH="/c/Program Files/PostgreSQL/18/bin:$PATH"; export PGPASSWORD="${PGPASSWORD:-$(grep '^DB_PASSWORD=' "$(dirname "$0")/../../src/main/resources/application-local.properties" | cut -d= -f2-)}"
API=http://localhost:8081
TMP=/c/Users/umveronica1/.java-tmp
psqlq(){ psql -U postgres -d boulder_db -t -A -c "$1"; }
ok=0; mal=0
chequear(){ if [ "$2" = "1" ]; then ok=$((ok+1)); echo "  OK    $1"; else mal=$((mal+1)); echo "  FALLO $1  -> $3"; fi; }

# --- usuario de prueba con rol GEOLOGO (texto plano: el primer login lo migra)
psqlq "delete from base.usuario_rol where id_usuario in (select id from base.usuario where usuario='prueba_sync'); delete from base.usuario where usuario='prueba_sync';" >/dev/null
psqlq "insert into base.usuario(id,nombres,apellidos,correo,usuario,contrasenia,estado) values (gen_random_uuid(),'Prueba','Sync','prueba.sync@x.pe','prueba_sync','clave123',true);" >/dev/null
psqlq "insert into base.usuario_rol(id,id_usuario,id_rol) select gen_random_uuid(),u.id,r.id from base.usuario u, base.rol r where u.usuario='prueba_sync' and r.codigo='GEOLOGO';" >/dev/null
TOK=$(curl -s -X POST $API/api/auth/login -H 'Content-Type: application/json' -d '{"usuario":"prueba_sync","contrasenia":"clave123"}' | python -c "import sys,json;print(json.load(sys.stdin).get('token',''))")
chequear "login devuelve token" "$([ -n "$TOK" ] && echo 1)" "sin token"

SON=$(psqlq "select id from operacional.prf_sondaje where codigo='DDH-VERO-S-040-26';")
LITO=$(psqlq "select id from geologia.lit_diccionario where codigo='GRD';")
ID=$(python -c "import uuid;print(uuid.uuid4())")

# --- 1. un navegador suscrito al canal SSE
rm -f $TMP/sse.txt
curl -s -N -H "Authorization: Bearer $TOK" $API/api/eventos > $TMP/sse.txt &
SSE_PID=$!
sleep 2
chequear "el canal SSE saluda al conectar" "$(grep -q 'event:conectado' $TMP/sse.txt && echo 1)" "$(head -c 200 $TMP/sse.txt)"

# --- 2. el tablet sube un tramo de quicklog
FILA='[{"id":"'$ID'","sondajeId":"'$SON'","desde":990.00,"hasta":992.00,"longitud":2.00,"litoId":"'$LITO'","fecha":"2026-09-28","geologo":"Prueba Sync"}]'
t0=$(date +%s%N)
COD=$(curl -s -o /dev/null -w "%{http_code}" -X PUT $API/api/logueo/quicklog -H "Authorization: Bearer $TOK" -H 'Content-Type: application/json' \
      -H 'X-Dispositivo: GEO-PRUEBA-01' -H 'X-App-Version: 0.9.0' -d "$FILA")
chequear "PUT quicklog desde el tablet" "$([ "$COD" = "204" ] && echo 1)" "HTTP $COD"
for i in $(seq 1 20); do grep -q 'event:cambio' $TMP/sse.txt && break; sleep 0.25; done
t1=$(date +%s%N)
chequear "el navegador recibe el aviso" "$(grep -q '"recurso":"quicklog"' $TMP/sse.txt && echo 1)" "$(tail -c 300 $TMP/sse.txt)"
chequear "el aviso trae el sondaje y el origen tablet" "$(grep 'event:cambio' -A1 $TMP/sse.txt | grep -q "$SON" && grep -q '"origen":"tablet"' $TMP/sse.txt && echo 1)" "$(grep -A1 'event:cambio' $TMP/sse.txt)"
echo "        latencia subida -> aviso: $(( (t1 - t0) / 1000000 )) ms"

# --- 3. la web lo ve
N=$(curl -s -H "Authorization: Bearer $TOK" "$API/api/logueo/quicklog?sondajeId=$SON" | python -c "import sys,json;print(sum(1 for f in json.load(sys.stdin) if f['id']=='$ID'))")
chequear "GET quicklog incluye el tramo nuevo" "$([ "$N" = "1" ] && echo 1)" "n=$N"

# --- 4. borrado lógico desde el tablet: la misma fila con eliminadoEn
BORRADA='[{"id":"'$ID'","sondajeId":"'$SON'","desde":990.00,"hasta":992.00,"longitud":2.00,"litoId":"'$LITO'","fecha":"2026-09-28","geologo":"Prueba Sync","eliminadoEn":"2026-09-28T23:30:00Z"}]'
curl -s -o /dev/null -X PUT $API/api/logueo/quicklog -H "Authorization: Bearer $TOK" -H 'Content-Type: application/json' -H 'X-Dispositivo: GEO-PRUEBA-01' -d "$BORRADA"
N=$(curl -s -H "Authorization: Bearer $TOK" "$API/api/logueo/quicklog?sondajeId=$SON" | python -c "import sys,json;print(sum(1 for f in json.load(sys.stdin) if f['id']=='$ID'))")
chequear "la web ya no ve el tramo borrado" "$([ "$N" = "0" ] && echo 1)" "n=$N"
N=$(curl -s -H "Authorization: Bearer $TOK" "$API/api/logueo/quicklog?sondajeId=$SON&incluirEliminados=true" | python -c "import sys,json;print(sum(1 for f in json.load(sys.stdin) if f['id']=='$ID' and f.get('eliminadoEn')))")
chequear "el tablet sí recibe la marca de borrado" "$([ "$N" = "1" ] && echo 1)" "n=$N"
V=$(psqlq "select count(*) from logueo.vw_mm_quicklog where quicklog_id='$ID';")
chequear "la vista de Micromine excluye el tramo borrado" "$([ "$V" = "0" ] && echo 1)" "n=$V"
A=$(psqlq "select actualizado_en > now() - interval '1 minute' from logueo.iquicklog where id='$ID';")
chequear "el trigger actualizó actualizado_en" "$([ "$A" = "t" ] && echo 1)" "$A"

# --- 5. registro del tablet
D=$(curl -s -H "Authorization: Bearer $TOK" $API/api/logueo/dispositivos | python -c "import sys,json;d=[x for x in json.load(sys.stdin) if x['dispositivo']=='GEO-PRUEBA-01'];print(d[0]['filasSubidas'] if d else -1, d[0]['usuario'] if d else '', d[0]['versionApp'] if d else '')")
chequear "el servidor registra el tablet, su usuario y sus filas" "$([ "$D" = "2 prueba_sync 0.9.0" ] && echo 1)" "$D"

# --- 6. sin token no hay canal
COD=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 $API/api/eventos)
chequear "el canal SSE exige token" "$([ "$COD" = "401" ] || [ "$COD" = "403" ] && echo 1)" "HTTP $COD"


# --- 7. borrar un tramo y loguear otro en el mismo metro, en UNA subida
IDV=$(python -c "import uuid;print(uuid.uuid4())"); IDN=$(python -c "import uuid;print(uuid.uuid4())")
curl -s -o /dev/null -X PUT $API/api/logueo/quicklog -H "Authorization: Bearer $TOK" -H 'Content-Type: application/json' -H 'X-Dispositivo: GEO-PRUEBA-01' \
  -d '[{"id":"'$IDV'","sondajeId":"'$SON'","desde":995.00,"hasta":997.00,"longitud":2.00,"litoId":"'$LITO'","fecha":"2026-09-28"}]'
LOTE='[{"id":"'$IDN'","sondajeId":"'$SON'","desde":995.00,"hasta":997.00,"longitud":2.00,"litoId":"'$LITO'","fecha":"2026-09-28"},{"id":"'$IDV'","sondajeId":"'$SON'","desde":995.00,"hasta":997.00,"longitud":2.00,"litoId":"'$LITO'","fecha":"2026-09-28","eliminadoEn":"2026-09-28T23:40:00Z"}]'
COD=$(curl -s -o /dev/null -w "%{http_code}" -X PUT $API/api/logueo/quicklog -H "Authorization: Bearer $TOK" -H 'Content-Type: application/json' -H 'X-Dispositivo: GEO-PRUEBA-01' -d "$LOTE")
chequear "borrar y reloguear el mismo metro en una subida (la baja viene 2da en el lote)" "$([ "$COD" = "204" ] && echo 1)" "HTTP $COD"
V=$(psqlq "select string_agg(case when eliminado_en is null then 'vigente' else 'borrado' end, ',' order by eliminado_en nulls last) from logueo.iquicklog where id in ('$IDV','$IDN');")
chequear "queda el nuevo vigente y el viejo borrado" "$([ "$V" = "borrado,vigente" ] && echo 1)" "$V"
T=$(curl -s -H "Authorization: Bearer $TOK" "$API/api/logueo/quicklog?sondajeId=$SON&incluirEliminados=true" | python -c "import sys,json;d=[f for f in json.load(sys.stdin) if f['id']=='$IDV'];print(type(d[0]['eliminadoEn']).__name__, d[0]['eliminadoEn'][:4])")
chequear "eliminadoEn viaja como texto ISO-8601 (lo que espera el tablet)" "$([ "$T" = "str 2026" ] && echo 1)" "$T"
CT=$(curl -s -o /dev/null -D - --max-time 2 -H "Authorization: Bearer $TOK" $API/api/eventos 2>/dev/null | grep -i "^content-type" | tr -d '\r')
chequear "el canal responde text/event-stream" "$(echo "$CT" | grep -qi "text/event-stream" && echo 1)" "$CT"
psqlq "delete from logueo.iquicklog where id in ('$IDV','$IDN');" >/dev/null
kill $SSE_PID 2>/dev/null
# --- limpieza
psqlq "delete from logueo.iquicklog where id='$ID'; delete from logueo.dispositivo_sync where dispositivo='GEO-PRUEBA-01'; delete from base.usuario_rol where id_usuario in (select id from base.usuario where usuario='prueba_sync'); delete from base.usuario where usuario='prueba_sync';" >/dev/null
echo "=== $ok OK / $mal FALLOS ==="
