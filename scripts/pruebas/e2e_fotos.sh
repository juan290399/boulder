#!/usr/bin/env bash
# Prueba de la API de fotos contra un backend levantado en el 8081 con
#   --boulder.fotos.directorio=C:/Users/umveronica1/.java-tmp/fotos-prueba
# Crea su propio usuario de prueba y deja la base como la encontró.
set -u
export PATH="/c/Program Files/PostgreSQL/18/bin:$PATH"; export PGPASSWORD="${PGPASSWORD:-$(grep '^DB_PASSWORD=' "$(dirname "$0")/../../src/main/resources/application-local.properties" | cut -d= -f2-)}"
API=http://localhost:8081
TMP=C:/Users/umveronica1/.java-tmp
psqlq(){ psql -U postgres -d boulder_db -t -A -c "$1"; }
ok=0; mal=0
chequear(){ if [ "$2" = "1" ]; then ok=$((ok+1)); echo "  OK    $1"; else mal=$((mal+1)); echo "  FALLO $1  -> $3"; fi; }

psqlq "delete from base.usuario_rol where id_usuario in (select id from base.usuario where usuario='prueba_fotos'); delete from base.usuario where usuario='prueba_fotos';" >/dev/null
psqlq "insert into base.usuario(id,nombres,apellidos,correo,usuario,contrasenia,estado) values (gen_random_uuid(),'Prueba','Fotos','prueba.fotos@x.pe','prueba_fotos','clave123',true);" >/dev/null
psqlq "insert into base.usuario_rol(id,id_usuario,id_rol) select gen_random_uuid(),u.id,r.id from base.usuario u, base.rol r where u.usuario='prueba_fotos' and r.codigo='GEOLOGO';" >/dev/null
TOK=$(curl -s -X POST $API/api/auth/login -H 'Content-Type: application/json' -d '{"usuario":"prueba_fotos","contrasenia":"clave123"}' | python -c "import sys,json;print(json.load(sys.stdin).get('token',''))")
SON=$(psqlq "select id from operacional.prf_sondaje where codigo='DDH-VERO-S-040-26';")

# Una foto de prueba de verdad: 2400x1200, simula una bandeja de testigo
python - "$TMP" <<'PY'
import sys
from PIL import Image, ImageDraw
im = Image.new("RGB", (2400, 1200), (60, 55, 50)); d = ImageDraw.Draw(im)
for i in range(5):
    d.rectangle([80, 100 + i*210, 2320, 250 + i*210], fill=(120 + i*15, 110, 95))
im.save(sys.argv[1] + "/foto_prueba.jpg", quality=88)
open(sys.argv[1] + "/no_es_foto.jpg", "w").write("esto es texto con extension .jpg")
PY

rm -f $TMP/sse_f.txt
curl -s -N -H "Authorization: Bearer $TOK" $API/api/eventos > $TMP/sse_f.txt &
SSE=$!; sleep 1.5

ID=$(python -c "import uuid;print(uuid.uuid4())")
subir(){ curl -s -w "\n%{http_code}" -X PUT "$API/api/fotos/$1" -H "Authorization: Bearer $TOK" -H 'X-Dispositivo: GEO-PRUEBA-01' \
  -F "archivo=@$2;type=image/jpeg" -F "sondajeId=$SON" -F "cajaDesde=133" -F "cajaHasta=142" -F "desde=247.25" -F "hasta=283.95" -F "condicion=seco"; }

R=$(subir $ID $TMP/foto_prueba.jpg); COD=$(echo "$R" | tail -1); CUERPO=$(echo "$R" | head -n -1)
chequear "subir una foto" "$([ "$COD" = "200" ] && echo 1)" "HTTP $COD $CUERPO"
DIM=$(echo "$CUERPO" | python -c "import sys,json;f=json.load(sys.stdin);print(f['ancho'],f['alto'],len(f['sha256']),f['tipoMime'])")
chequear "el servidor lee dimensiones, hash y tipo" "$([ "$DIM" = "2400 1200 64 image/jpeg" ] && echo 1)" "$DIM"

R2=$(subir $ID $TMP/foto_prueba.jpg); N=$(psqlq "select count(*) from operacional.fot_foto where id='$ID';")
chequear "reintentar la misma subida no duplica" "$([ "$(echo "$R2" | tail -1)" = "200" ] && [ "$N" = "1" ] && echo 1)" "n=$N"

COD=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$API/api/fotos/$(python -c 'import uuid;print(uuid.uuid4())')" -H "Authorization: Bearer $TOK" \
  -F "archivo=@$TMP/no_es_foto.jpg;type=image/jpeg" -F "sondajeId=$SON")
chequear "rechaza un archivo que no es imagen aunque diga .jpg" "$([ "$COD" = "400" ] && echo 1)" "HTTP $COD"

COD=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$API/api/fotos/$(python -c 'import uuid;print(uuid.uuid4())')" -H "Authorization: Bearer $TOK" \
  -F "archivo=@$TMP/foto_prueba.jpg;type=image/jpeg" -F "sondajeId=$SON" -F "desde=300" -F "hasta=200")
chequear "rechaza un tramo invertido" "$([ "$COD" = "400" ] && echo 1)" "HTTP $COD"

L=$(curl -s -H "Authorization: Bearer $TOK" "$API/api/fotos?sondajeId=$SON" | python -c "import sys,json;print(sum(1 for f in json.load(sys.stdin) if f['id']=='$ID'))")
chequear "la galería lista la foto" "$([ "$L" = "1" ] && echo 1)" "n=$L"

curl -s -o $TMP/min.jpg -H "Authorization: Bearer $TOK" "$API/api/fotos/$ID/miniatura"
curl -s -o $TMP/orig.jpg -H "Authorization: Bearer $TOK" "$API/api/fotos/$ID/contenido"
MIN=$(python -c "from PIL import Image;print(Image.open('$TMP/min.jpg').size)")
chequear "la miniatura mide 480 px de ancho" "$([ "$MIN" = "(480, 240)" ] && echo 1)" "$MIN"
chequear "el original llega idéntico" "$(cmp -s $TMP/orig.jpg $TMP/foto_prueba.jpg && echo 1)" "difiere"

COD=$(curl -s -o /dev/null -w "%{http_code}" "$API/api/fotos/$ID/contenido")
chequear "sin token no se ve la foto" "$([ "$COD" = "401" ] || [ "$COD" = "403" ] && echo 1)" "HTTP $COD"

chequear "la subida avisa por el canal en vivo" "$(grep -q '"recurso":"foto"' $TMP/sse_f.txt && echo 1)" "$(tail -c 200 $TMP/sse_f.txt)"

FOTO=$(curl -s -H "Authorization: Bearer $TOK" "$API/api/operacional/frente-avance?soloActivos=false" | python -c "
import sys,json
for s in json.load(sys.stdin):
    if s['sondajeId']=='$SON':
        print([e['profundidad'] for e in s['etapas'] if e['clave']=='fotografia'][0])")
chequear "el frente de avance toma el tramo de la foto (283.95)" "$(python -c "print(1 if abs(float('$FOTO')-283.95)<0.01 else 0)" 2>/dev/null | grep -q 1 && echo 1)" "fotografiado=$FOTO"

COD=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE -H "Authorization: Bearer $TOK" "$API/api/fotos/$ID")
L=$(curl -s -H "Authorization: Bearer $TOK" "$API/api/fotos?sondajeId=$SON" | python -c "import sys,json;print(sum(1 for f in json.load(sys.stdin) if f['id']=='$ID'))")
ARCH=$(ls $TMP/fotos-prueba/DDH-VERO-S-040-26/$ID.jpg 2>/dev/null | wc -l)
chequear "borrar la quita de la galería y conserva el archivo" "$([ "$COD" = "204" ] && [ "$L" = "0" ] && [ "$ARCH" = "1" ] && echo 1)" "HTTP $COD n=$L archivo=$ARCH"

kill $SSE 2>/dev/null
psqlq "delete from operacional.fot_foto where id='$ID'; delete from logueo.dispositivo_sync where dispositivo='GEO-PRUEBA-01'; delete from base.usuario_rol where id_usuario in (select id from base.usuario where usuario='prueba_fotos'); delete from base.usuario where usuario='prueba_fotos';" >/dev/null
rm -f "$TMP/fotos-prueba/DDH-VERO-S-040-26/$ID.jpg" "$TMP/fotos-prueba/DDH-VERO-S-040-26/${ID}_min.jpg"
echo "=== $ok OK / $mal FALLOS ==="
