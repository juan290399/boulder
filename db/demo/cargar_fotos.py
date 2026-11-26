"""
Carga fotos de testigo en boulder_demo para la evaluación.

Toma fotos reales de cajas portatestigo (una carpeta por sondaje, en orden de
toma) y las asigna a los sondajes ficticios DDH-COND-N-001-26 … 009: la
carpeta n.º k va al sondaje n.º k. Hace lo mismo que FotoService.subir():

- reduce la imagen a 2000 px de ancho (las originales pesan ~7 MB),
- guarda el archivo y su miniatura de 480 px en <destino>/<código>/<id>.jpg,
- inserta la fila en operacional.fot_foto con su hash, tamaño y medidas.

Las fotos van en pares seco/húmedo de las mismas dos cajas (≈ 6,2 m), como se
toman en el almacén, y solo hasta donde llegó la fotografía en cada sondaje.

Uso:
    python cargar_fotos.py <carpeta_origen> <carpeta_destino> > fotos_demo.sql
    psql -d boulder_demo -f fotos_demo.sql
"""
import hashlib
import io
import sys
import uuid
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image, ImageOps

NS = uuid.UUID('6f1c2d3e-0000-4000-8000-000000000026')
METROS_CAJA = 3.1
FOTOS_POR_SONDAJE = 36
# Hasta dónde llegó la fotografía en cada sondaje (ver generar_demo.py).
FOTOGRAFIADO = {1: 520.0, 2: 451.2, 3: 600.0, 4: 482.5, 5: 550.0, 6: 400.0, 7: 403.6, 8: 226.4, 9: 76.3}
GEOLOGO = 'José Puma'


def jpeg(imagen: Image.Image, ancho: int, calidad: int) -> bytes:
    if imagen.width > ancho:
        alto = round(imagen.height * ancho / imagen.width)
        imagen = imagen.resize((ancho, alto), Image.LANCZOS)
    salida = io.BytesIO()
    imagen.convert('RGB').save(salida, 'JPEG', quality=calidad, optimize=True)
    return salida.getvalue()


def main(origen: Path, destino: Path) -> None:
    sys.stdout.reconfigure(encoding='utf-8')
    carpetas = sorted(p for p in origen.iterdir() if p.is_dir())
    print('BEGIN;')
    print('DELETE FROM operacional.fot_foto;')
    for n, carpeta in enumerate(carpetas[:9], start=1):
        codigo = f'DDH-COND-N-{n:03d}-26'
        sondaje = f"(SELECT id FROM operacional.prf_sondaje WHERE codigo = '{codigo}')"
        cajas_max = int(FOTOGRAFIADO[n] // METROS_CAJA)
        archivos = sorted(carpeta.glob('*.JPG')) + sorted(carpeta.glob('*.jpg'))
        (destino / codigo).mkdir(parents=True, exist_ok=True)
        tomada = datetime(2026, 8, 3, 9, 0, tzinfo=timezone.utc) + timedelta(days=(n - 1) * 9)
        for i, archivo in enumerate(archivos[:FOTOS_POR_SONDAJE]):
            par = i // 2
            caja_desde = par * 2 + 1
            caja_hasta = caja_desde + 1
            if caja_hasta > cajas_max:
                break
            condicion = 'seco' if i % 2 == 0 else 'humedo'
            idf = uuid.uuid5(NS, f'foto/{codigo}/{i}')
            with Image.open(archivo) as original:
                imagen = ImageOps.exif_transpose(original)
                contenido = jpeg(imagen, 2000, 82)
                miniatura = jpeg(imagen, 480, 80)
                ancho, alto = Image.open(io.BytesIO(contenido)).size
            clave = f'{codigo}/{idf}.jpg'
            (destino / clave).write_bytes(contenido)
            (destino / f'{codigo}/{idf}_min.jpg').write_bytes(miniatura)
            desde = round((caja_desde - 1) * METROS_CAJA, 2)
            hasta = round(caja_hasta * METROS_CAJA, 2)
            momento = (tomada + timedelta(hours=par * 2, minutes=i % 2 * 3)).isoformat()
            print("INSERT INTO operacional.fot_foto (id, sondaje_id, caja_desde, caja_hasta, desde, hasta, condicion, "
                  "tomada_en, autor, dispositivo, archivo_original, tipo_mime, bytes, ancho, alto, sha256, "
                  "clave_almacen, creado_en, actualizado_en) VALUES "
                  f"('{idf}', {sondaje}, {caja_desde}, {caja_hasta}, {desde}, {hasta}, '{condicion}', '{momento}', "
                  f"'{GEOLOGO}', 'Cámara almacén', '{archivo.name}', 'image/jpeg', {len(contenido)}, {ancho}, {alto}, "
                  f"'{hashlib.sha256(contenido).hexdigest()}', '{clave}', now(), now());")
        print(f'-- {codigo}: fotos de {carpeta.name}', file=sys.stderr)
    print('COMMIT;')


if __name__ == '__main__':
    main(Path(sys.argv[1]), Path(sys.argv[2]))
