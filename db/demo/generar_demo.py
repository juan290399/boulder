"""
Genera datos_demo.sql: una campaña de perforación ficticia para la base
boulder_demo, pensada para la evaluación del Curso Integrador I.

Los datos son inventados pero siguen la lógica de Boulder, de modo que cada
pantalla muestre algo que explicar:

- Proyecto CÓNDOR (CND), UTM 18S, con 6 plataformas y 3 máquinas.
- 10 sondajes DDH-COND-N-001-26 … 010: seis terminados, tres en perforación y
  uno programado sin iniciar.
- Partes diarios de perforación por turno, con su personal, y las etapas que
  siguen (recepción de cajas, logueo geológico, geotécnico y estructural,
  fotografía, corte, muestreo y densidad) con atrasos crecientes: en Avances
  se ve dónde se corta la cadena en cada sondaje.
- Quicklog continuo de 0 m a lo logueado (pórfido de Cu-Mo con skarn), con un
  hueco a propósito en DDH-COND-N-004-26 para que aparezca «m sin describir».
- Estaciones de survey y mediciones de desvío con decisiones y seguimiento.

Uso (desde esta carpeta):
    python generar_demo.py            # escribe datos_demo.sql
    psql -d boulder_demo -f datos_demo.sql

Requiere que boulder_demo tenga el esquema de boulder_db y los catálogos de
geología, roles, cargos y usuarios (ver LEAME.md). Es determinista: la misma
semilla da siempre los mismos datos.
"""
import math
import random
import uuid
from datetime import date, timedelta
from pathlib import Path

random.seed(2026)
NS = uuid.UUID('6f1c2d3e-0000-4000-8000-000000000026')
# Último día con partes: ayer respecto de la evaluación (evita fechas «en el
# futuro» por la diferencia horaria entre el servidor y el navegador).
HOY = date(2026, 11, 25)


def uid(*partes) -> str:
    return str(uuid.uuid5(NS, '/'.join(str(p) for p in partes)))


def q(v) -> str:
    """Literal SQL."""
    if v is None:
        return 'NULL'
    if isinstance(v, bool):
        return 'true' if v else 'false'
    if isinstance(v, (int,)):
        return str(v)
    if isinstance(v, float):
        return f'{v:.4f}'.rstrip('0').rstrip('.') if abs(v) < 1e9 else f'{v:.2f}'
    if isinstance(v, date):
        return f"'{v.isoformat()}'"
    if isinstance(v, Raw):
        return v.sql
    return "'" + str(v).replace("'", "''") + "'"


class Raw:
    def __init__(self, sql):
        self.sql = sql


def cat(tabla, codigo):
    """Id de una entrada de catálogo por su código (los códigos son char con relleno)."""
    return Raw(f"(SELECT id FROM geologia.{tabla} WHERE trim(codigo) = '{codigo}')")


SQL = []


def insert(tabla, filas):
    if not filas:
        return
    cols = list(filas[0].keys())
    SQL.append(f'INSERT INTO {tabla} ({", ".join(cols)}) VALUES')
    SQL.append(',\n'.join('(' + ', '.join(q(f[c]) for c in cols) + ')' for f in filas) + ';')


def r2(x):
    return round(x, 2)


# ------------------------------------------------------------------ maestros
EMP_MINERA = uid('empresa', 'minera')
EMP_PERF = uid('empresa', 'perforaciones')
insert('base.empresa', [
    dict(id=EMP_MINERA, codigo='MCONDOR', nombre='Minera Cóndor Andino S.A.C.', ruc='20612345678',
         direccion='Av. Los Volcanes 410, Arequipa', correo='operaciones@condorandino.pe', telefono='054 231 400'),
    dict(id=EMP_PERF, codigo='PALTIPLANO', nombre='Perforaciones Altiplano S.A.C.', ruc='20698765432',
         direccion='Calle Las Canteras 122, Cerro Colorado', correo='campo@paltiplano.pe', telefono='054 448 120'),
])
PROY = uid('proyecto', 'CND')
insert('base.proyecto', [dict(id=PROY, id_empresa=EMP_MINERA, codigo='CND', nombre='Cóndor',
                              ubicacion='Arequipa - Caylloma - Tisco', srid=32718)])

CARGO = {c: Raw(f"(SELECT id FROM base.cargo WHERE codigo = '{c}')") for c in
         ['PERFORISTA', 'AYUDANTE_PERF', 'SUPERVISOR_PERF', 'GEOLOGO', 'AYUDANTE_GEO', 'JEFE_GEO', 'TECNICO_LAB']}
personas = [
    ('PERF-RQUI', 'Rubén', 'Quispe Mamani', 'PERFORISTA', EMP_PERF),
    ('PERF-HCAL', 'Hugo', 'Calla Ccori', 'PERFORISTA', EMP_PERF),
    ('PERF-WTIC', 'Wilber', 'Ticona Apaza', 'PERFORISTA', EMP_PERF),
    ('PERF-ELUQ', 'Edgar', 'Luque Condori', 'PERFORISTA', EMP_PERF),
    ('AYU-JHUA', 'Jhon', 'Huamaní Cutipa', 'AYUDANTE_PERF', EMP_PERF),
    ('AYU-FPAC', 'Freddy', 'Pacheco Ramos', 'AYUDANTE_PERF', EMP_PERF),
    ('AYU-DCHO', 'David', 'Choque Flores', 'AYUDANTE_PERF', EMP_PERF),
    ('AYU-MVIL', 'Marco', 'Villca Sucso', 'AYUDANTE_PERF', EMP_PERF),
    ('AYU-LARO', 'Luis', 'Aroni Taco', 'AYUDANTE_PERF', EMP_PERF),
    ('AYU-PMAM', 'Percy', 'Mamani Larico', 'AYUDANTE_PERF', EMP_PERF),
    ('SUP-ATAP', 'Alberto', 'Tapia Gómez', 'SUPERVISOR_PERF', EMP_PERF),
    ('GEO-MROJ', 'María', 'Rojas Herrera', 'JEFE_GEO', EMP_MINERA),
    ('GEO-CVAL', 'Carla', 'Valdivia Núñez', 'GEOLOGO', EMP_MINERA),
    ('GEO-RSAL', 'Renzo', 'Salas Paredes', 'GEOLOGO', EMP_MINERA),
    ('GEO-KCHA', 'Karen', 'Chávez Zúñiga', 'GEOLOGO', EMP_MINERA),
    ('AGEO-JPUM', 'José', 'Puma Chura', 'AYUDANTE_GEO', EMP_MINERA),
    ('LAB-SMED', 'Silvia', 'Medina Quispe', 'TECNICO_LAB', EMP_MINERA),
]
EMP = {}
filas = []
for cod, nom, ape, cargo, empresa in personas:
    EMP[cod] = (uid('empleado', cod), f'{nom} {ape.split()[0]}')
    filas.append(dict(id=EMP[cod][0], codigo=cod, nombres=nom, apellidos=ape,
                      documento=str(random.randint(40000000, 79999999)), empresa_id=empresa,
                      cargo_id=CARGO[cargo], email=f'{nom.lower()}.{ape.split()[0].lower()}@demo.pe'
                      .replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u'),
                      telefono=f'9{random.randint(10000000, 99999999)}', fecha_ingreso=date(2026, 7, 15),
                      fecha_salida=None, estado=True, notas=None))
# Una baja para que se vea el estado en Empleados.
filas[9]['estado'] = False
filas[9]['fecha_salida'] = date(2026, 10, 20)
insert('base.empleado', filas)
GEOLOGOS = ['GEO-CVAL', 'GEO-RSAL', 'GEO-KCHA']
PERFORISTAS = ['PERF-RQUI', 'PERF-HCAL', 'PERF-WTIC', 'PERF-ELUQ']
AYUDANTES = ['AYU-JHUA', 'AYU-FPAC', 'AYU-DCHO', 'AYU-MVIL', 'AYU-LARO']

MAQ = {
    'LF90-1': dict(id=uid('maq', 'LF90-1'), codigo='LF90-1', nombre='Boart Longyear LF90 N.º 1', tipo='Diamantina',
                   alcance_pq=450, alcance_hq=800, alcance_nq=1200, marca='Boart Longyear', motor='Cummins 6BTA',
                   fecha_compra=date(2019, 3, 1), fecha_llegada=date(2026, 7, 20), fecha_inicio=date(2026, 8, 1),
                   estado='Operativa', notas=None),
    'LF90-2': dict(id=uid('maq', 'LF90-2'), codigo='LF90-2', nombre='Boart Longyear LF90 N.º 2', tipo='Diamantina',
                   alcance_pq=450, alcance_hq=800, alcance_nq=1200, marca='Boart Longyear', motor='Cummins 6BTA',
                   fecha_compra=date(2020, 6, 1), fecha_llegada=date(2026, 8, 25), fecha_inicio=date(2026, 9, 1),
                   estado='Operativa', notas=None),
    'CS14-1': dict(id=uid('maq', 'CS14-1'), codigo='CS14-1', nombre='Atlas Copco CS14', tipo='Diamantina',
                   alcance_pq=300, alcance_hq=600, alcance_nq=900, marca='Atlas Copco', motor='Deutz BF4M',
                   fecha_compra=date(2017, 1, 1), fecha_llegada=date(2026, 9, 25), fecha_inicio=date(2026, 10, 1),
                   estado='En mantenimiento', notas='Cambio de cabezal programado para la primera semana de diciembre.'),
}
insert('operacional.prf_maquina', list(MAQ.values()))
insert('operacional.prf_proyecto_maquina', [
    dict(id=uid('pm', k), proyecto_id=PROY, maquina_id=m['id'], fecha_ingreso=m['fecha_inicio'], fecha_salida=None,
         estado='activa') for k, m in MAQ.items()])

E0, N0 = 812350.0, 8278420.0   # UTM 18S, zona de Tisco (ficticia)
PLAT = {}
filas = []
for i, (estado, tipo) in enumerate([('Concluido', 'Superficie'), ('Concluido', 'Superficie'), ('Operativo', 'Superficie'),
                                    ('Operativo', 'Superficie'), ('Operativo', 'Ladera'), ('En construccion', 'Ladera')], 1):
    cod = f'PLAT-C{i:02d}'
    e, n = E0 + (i % 3) * 180 + random.uniform(-20, 20), N0 + (i // 3) * 210 + random.uniform(-20, 20)
    PLAT[cod] = dict(id=uid('plat', cod), e=e, n=n, z=4310 + i * 6.5)
    filas.append(dict(id=PLAT[cod]['id'], proyecto_id=PROY, codigo=cod, nombre=f'Plataforma Cóndor {i:02d}',
                      este=r2(e), norte=r2(n), elevacion=r2(PLAT[cod]['z']), eje_mayor=20.0, eje_menor=15.0,
                      eje_z=1.5, area=300.0, numero_pozas=2, estado=estado, tipo=tipo,
                      ubicacion='Sector Norte, quebrada Cóndor', notas=None))
insert('operacional.prf_plataforma', filas)

# --------------------------------------------------------------- sondajes
# (n, plataforma, máquina, prof. programada, az, inc, inicio, fin o None, prof. actual)
PLAN = [
    (1, 'PLAT-C01', 'LF90-1', 520, 135, -65, date(2026, 8, 2), date(2026, 8, 24), 520.0),
    (2, 'PLAT-C01', 'LF90-1', 450, 315, -70, date(2026, 8, 26), date(2026, 9, 14), 451.2),
    (3, 'PLAT-C02', 'LF90-2', 600, 90, -60, date(2026, 9, 2), date(2026, 9, 28), 600.0),
    (4, 'PLAT-C02', 'LF90-1', 480, 180, -75, date(2026, 9, 16), date(2026, 10, 6), 482.5),
    (5, 'PLAT-C03', 'LF90-2', 550, 45, -65, date(2026, 9, 30), date(2026, 10, 26), 550.0),
    (6, 'PLAT-C03', 'CS14-1', 400, 225, -55, date(2026, 10, 3), date(2026, 10, 25), 400.0),
    (7, 'PLAT-C04', 'LF90-1', 650, 120, -70, date(2026, 11, 2), None, 412.6),
    (8, 'PLAT-C04', 'LF90-2', 500, 300, -60, date(2026, 11, 11), None, 238.4),
    (9, 'PLAT-C05', 'CS14-1', 420, 160, -80, date(2026, 11, 20), None, 96.3),
    (10, 'PLAT-C06', None, 500, 90, -65, None, None, 0),
]
SON = {}
progs, collares, sondajes = [], [], []
for n, plat, maq, prof, az, inc, ini, fin, actual in PLAN:
    cod = f'DDH-COND-N-{n:03d}-26'
    p = PLAT[plat]
    e, no, z = p['e'] + random.uniform(-6, 6), p['n'] + random.uniform(-6, 6), p['z']
    SON[cod] = dict(id=uid('son', cod), n=n, plat=plat, maq=maq, prof=prof, az=az, inc=inc, ini=ini, fin=fin,
                    actual=actual)
    progs.append(dict(id=uid('prog', cod), proyecto_id=PROY, plataforma_id=p['id'], codigo=f'P-CND-N-{n:04d}-26',
                      nombre='Programa Cóndor 2026', codigo_original=f'CND-26-{n:03d}', este=r2(e), norte=r2(no),
                      elevacion=r2(z), profundidad=float(prof), azimut=float(az), inclinacion=float(inc), plano=None,
                      punto=None, prioridad='Alta' if n <= 6 else 'Media', zona='Sector Norte', flanco=None, notas=None))
    collares.append(dict(id=uid('col', cod), codigo=f'C-{cod}', nombre=f'C-{n:03d}-26', este=r2(e), norte=r2(no),
                         elevacion=r2(z), fecha_medicion=ini, campania='Cóndor 2026', notas=None))
# Dos puntos programados que aún no tienen sondaje.
for n in (11, 12):
    p = PLAT['PLAT-C06']
    progs.append(dict(id=uid('prog', n), proyecto_id=PROY, plataforma_id=p['id'], codigo=f'P-CND-N-{n:04d}-26',
                      nombre='Programa Cóndor 2026', codigo_original=f'CND-26-{n:03d}', este=r2(p['e'] + n),
                      norte=r2(p['n'] - n), elevacion=r2(p['z']), profundidad=450.0, azimut=270.0, inclinacion=-60.0,
                      plano=None, punto=None, prioridad='Baja', zona='Sector Norte', flanco=None, notas=None))
insert('operacional.prf_programa_sondaje', progs)
insert('operacional.prf_collar', collares)


# ------------------------------------------------------ partes de perforación
def linea_para(prof):
    return 'PQ' if prof < 30 else ('HQ3' if prof < 280 else 'NQ3')


avances, personal = [], []
for cod, s in SON.items():
    if not s['ini']:
        continue
    hasta_dia = s['fin'] or HOY - timedelta(days=1)
    objetivo = s['actual']
    dias = (hasta_dia - s['ini']).days + 1
    ritmo = objetivo / (dias * 2)
    prof = 0.0
    k = 0
    for d in range(dias):
        f = s['ini'] + timedelta(days=d)
        for turno in ('dia', 'noche'):
            if prof >= objetivo - 0.01:
                break
            # Algún turno parado por mantenimiento o cambio de línea.
            avance = 0.0 if random.random() < 0.06 else max(2.0, random.gauss(ritmo, ritmo * 0.25))
            if d == dias - 1 and turno == 'noche':
                avance = objetivo - prof          # el último turno cierra en la profundidad final
            avance = min(avance, objetivo - prof)
            k += 1
            aid = uid('prf', cod, k)
            perforista = PERFORISTAS[(s['n'] + (0 if turno == 'dia' else 1)) % 4]
            avances.append(dict(id=aid, sondaje_id=s['id'], sondaje_codigo=cod, fecha=f, turno=turno,
                                responsable=EMP[perforista][1], maquina_id=MAQ[s['maq']]['id'], maquina_codigo=s['maq'],
                                plataforma_id=PLAT[s['plat']]['id'], plataforma_codigo=s['plat'], desde=r2(prof),
                                hasta=r2(prof + avance), avance=r2(avance),
                                casing_tipo='PWT' if prof < 12 else None, casing_desde=0.0 if prof < 12 else None,
                                casing_hasta=12.0 if prof < 12 else None,
                                retorno_pct=float(random.choice([100, 100, 95, 90, 80, 70])), linea=linea_para(prof),
                                comentarios='Parada por cambio de corona' if avance == 0 else None,
                                estado='registrado', observaciones=None))
            for j, (pid, cargo) in enumerate([(perforista, 'perforista'),
                                              (AYUDANTES[(s['n'] + k) % 5], 'ayudante'),
                                              (AYUDANTES[(s['n'] + k + 2) % 5], 'ayudante')]):
                personal.append(dict(id=uid('per', cod, k, j), avance_id=aid, perforista_id=EMP[pid][0],
                                     nombre=EMP[pid][1], cargo=cargo, codigo_maquina=s['maq']))
            prof += avance
    s['actual'] = r2(prof)
    s['dias'] = dias
insert('operacional.prf_avance', avances)
insert('operacional.prf_avance_personal', personal)

for cod, s in SON.items():
    sondajes.append(dict(id=s['id'], proyecto_id=PROY, programa_id=uid('prog', cod), collar_id=uid('col', cod),
                         plataforma_id=PLAT[s['plat']]['id'], maquina_id=MAQ[s['maq']]['id'] if s['maq'] else None,
                         codigo=cod, nombre=f'DDH-{s["n"]:03d}', azimut_setup=float(s['az']),
                         inclinacion_setup=float(s['inc']), profundidad_actual=float(s['actual']),
                         profundidad_final=float(s['prof']), medicion_utilizada=None, mediciones_totales=None,
                         fecha_inicio=s['ini'], fecha_fin=s['fin'], campania='Cóndor 2026', notas=None))
# El sondaje va antes que sus partes: se inserta primero en el archivo final.
SQL_SONDAJES = len(SQL)

# ----------------------------------------------- etapas aguas abajo (atrasos)
# Fracción de lo perforado que cada etapa alcanzó, según días transcurridos.
def alcance(s, atraso_dias, ritmo_dia):
    if not s['ini']:
        return 0.0
    if s['fin'] and (HOY - s['fin']).days > atraso_dias + 6:
        return s['actual']
    dias_utiles = (HOY - s['ini']).days - atraso_dias
    return r2(max(0.0, min(s['actual'], dias_utiles * ritmo_dia)))


def partes_por_tramo(cod, s, hasta, ritmo, prefijo):
    """Corta [0, hasta] en partes diarios de largo parecido."""
    salida, prof, d = [], 0.0, 0
    while prof < hasta - 0.01:
        paso = min(hasta - prof, max(5.0, random.gauss(ritmo, ritmo * 0.3)))
        salida.append((s['ini'] + timedelta(days=d + 1), r2(prof), r2(prof + paso)))
        prof += paso
        d += 1
    return salida


rec, logs, fots, cortes, mues, dens = [], [], [], [], [], []
ETAPAS = dict(recepcion=(1, 26), geologico=(4, 24), geotecnico=(5, 26), estructural=(9, 20),
              fotografia=(3, 25), corte=(10, 22), muestreo=(13, 21), densidad=(18, 18))
# Metros que cada etapa va detrás de lo perforado. Los primeros cuatro sondajes
# están cerrados en todas las etapas; los recientes muestran la cadena cortada.
ATRASO = {
    5: dict(densidad=170, muestreo=40),
    6: dict(densidad=140, muestreo=60, corte=20),
    7: dict(recepcion=3, fotografia=9, geologico=32, geotecnico=21, estructural=115, corte=150, muestreo=196,
            densidad=262),
    8: dict(recepcion=4, fotografia=12, geologico=45, geotecnico=30, estructural=130, corte=160, muestreo=238,
            densidad=238),
    9: dict(recepcion=6, fotografia=20, geologico=38, geotecnico=50, estructural=96, corte=96, muestreo=96,
            densidad=96),
}
for cod, s in SON.items():
    if not s['ini']:
        continue
    atraso = ATRASO.get(s['n'], {})
    lim = {k: r2(max(0.0, s['actual'] - atraso.get(k, 0))) for k in ETAPAS}
    lim = {k: (v if v >= 1 else 0.0) for k, v in lim.items()}
    for k in ('geologico', 'geotecnico', 'estructural', 'fotografia'):
        lim[k] = min(lim[k], lim['recepcion'])
    lim['corte'] = min(lim['corte'], lim['geologico'], lim['fotografia'])
    lim['muestreo'] = min(lim['muestreo'], lim['corte'])
    lim['densidad'] = min(lim['densidad'], lim['corte'])
    s['lim'] = lim
    caja = 0
    for i, (f, a, b) in enumerate(partes_por_tramo(cod, s, lim['recepcion'], 26, 'rec')):
        c1, c2 = caja + 1, caja + max(1, round((b - a) / 3.1))
        caja = c2
        rec.append(dict(id=uid('rec', cod, i), sondaje_id=s['id'], sondaje_codigo=cod, fecha=min(f, HOY), turno='dia',
                        responsable=EMP['AGEO-JPUM'][1], box_desde=c1, box_hasta=c2, total_cajas=c2 - c1 + 1,
                        marcado_desde=a, marcado_hasta=b, total_metraje=r2(b - a), estado='registrado',
                        observaciones=None))
    for tipo in ('geologico', 'geotecnico', 'estructural'):
        for i, (f, a, b) in enumerate(partes_por_tramo(cod, s, lim[tipo], ETAPAS[tipo][1], tipo)):
            logs.append(dict(id=uid('log', cod, tipo, i), sondaje_id=s['id'], sondaje_codigo=cod,
                             fecha=min(f + timedelta(days=ETAPAS[tipo][0]), HOY), turno='dia', tipo_logueo=tipo,
                             desde=a, hasta=b, avance=r2(b - a), geologo=EMP[GEOLOGOS[(s['n'] + i) % 3]][1],
                             pct_completado=100.0, estado='registrado', observaciones=None))
    caja = 0
    for i, (f, a, b) in enumerate(partes_por_tramo(cod, s, lim['fotografia'], 25, 'fot')):
        c1, c2 = caja + 1, caja + max(1, round((b - a) / 3.1))
        caja = c2
        fots.append(dict(id=uid('fot', cod, i), sondaje_id=s['id'], sondaje_codigo=cod,
                         fecha=min(f + timedelta(days=3), HOY), turno='dia', responsable=EMP['AGEO-JPUM'][1],
                         box_desde=c1, box_hasta=c2, total_cajas=c2 - c1 + 1, marcado_desde=a, marcado_hasta=b,
                         total_metraje=r2(b - a), seco_humedo='humedo' if i % 2 else 'seco',
                         nro_fotos=(c2 - c1 + 1) * 2, ruta_archivos=None, estado='registrado', observaciones=None))
    for i, (f, a, b) in enumerate(partes_por_tramo(cod, s, lim['corte'], 22, 'crt')):
        cortes.append(dict(id=uid('crt', cod, i), sondaje_id=s['id'], sondaje_codigo=cod,
                           fecha=min(f + timedelta(days=10), HOY), turno='dia', responsable=EMP['LAB-SMED'][1],
                           desde=a, hasta=b, tipo_linea=linea_para(a), metraje_estimado=r2(b - a),
                           nro_muestras=max(1, round((b - a) / 2)), estado='registrado', observaciones=None))
    for i, (f, a, b) in enumerate(partes_por_tramo(cod, s, lim['muestreo'], 21, 'mst')):
        n = max(1, round((b - a) / 2))
        mues.append(dict(id=uid('mst', cod, i), sondaje_id=s['id'], sondaje_codigo=cod,
                         fecha=min(f + timedelta(days=13), HOY), turno='dia', responsable=EMP['LAB-SMED'][1],
                         estado_muestreo='finalizado', desde=a, hasta=b, avance=r2(b - a), nro_muestras=n,
                         nro_muestras_embolsadas=n, nro_costales=max(1, n // 4), nro_lotes=1, estado='registrado',
                         observaciones=None))
    for i, (f, a, b) in enumerate(partes_por_tramo(cod, s, lim['densidad'], 18, 'dns')):
        dens.append(dict(id=uid('dns', cod, i), sondaje_id=s['id'], sondaje_codigo=cod,
                         fecha=min(f + timedelta(days=18), HOY), turno='dia', responsable=EMP['LAB-SMED'][1],
                         desde=a, hasta=b, total_muestras=max(1, round((b - a) / 10)), total_metraje=r2(b - a),
                         tipo_linea=linea_para(a), estado='registrado', observaciones=None))
insert('operacional.log_recepcion_caja', rec)
insert('operacional.log_avance', logs)
insert('operacional.fot_avance', fots)
insert('operacional.crt_muestra', cortes)
insert('operacional.mst_avance', mues)
insert('operacional.crt_densidad', dens)

# ---------------------------------------------------------------- quicklog
# Columna tipo de un pórfido de Cu-Mo que corta calizas con skarn.
def columna(s):
    """Tramos (desde, hasta, lito, alt1, int1, alt2, int2, minerales, zona, %py, %cu, %mo)."""
    prof, tramos = 0.0, []
    techo_ox = random.uniform(30, 55)
    techo_prim = techo_ox + random.uniform(35, 70)
    while prof < s['prof']:
        if prof < random.uniform(4, 9) and not tramos:
            t = ('ALV', 'OXCI', 'FUE', None, None, ['LIM', 'GOE'], 'OXID', 0, 0, 0, random.uniform(3, 6))
        else:
            r = random.random()
            if r < 0.07:
                t = ('ZFA', 'ARGI', 'FUE', None, None, ['ARC', 'PYT'], None, 2, 0.05, 0, random.uniform(0.4, 2.0))
            elif r < 0.13:
                t = ('DQZ', 'SILC', 'INS', None, None, ['CUZ', 'PYT', 'MOL'], None, 1, 0.1, 0.05, random.uniform(0.5, 3))
            elif r < 0.30:
                t = ('CLZ' if random.random() < 0.5 else 'SGR', 'SKPR', 'MOD', 'SKRE', 'DEB', ['GRT', 'MGT', 'CPY'],
                     None, 1, 0.4, 0, random.uniform(4, 22))
            elif r < 0.45:
                t = ('HBX', 'FILI', 'FUE', 'SILC', 'MOD', ['PYT', 'CPY', 'MOL'], None, 4, 0.6, 0.08, random.uniform(3, 12))
            else:
                t = ('GRD' if random.random() < 0.6 else 'MZD', 'POTA' if prof > techo_prim + 60 else 'FILI',
                     'MOD', 'CLTZ', 'DEB', ['CPY', 'PYT', 'MOL'], None, 2, 0.45, 0.03, random.uniform(6, 30))
        lito, a1, i1, a2, i2, mins, zona, py, cu, mo, largo = t
        hasta = min(s['prof'], prof + largo)
        if zona is None:
            zona = 'OXID' if prof < techo_ox else ('SFSC' if prof < techo_prim else 'SFPR')
        if zona == 'SFSC' and lito not in ('ZFA',):
            mins = ['CCL', 'CVL', 'PYT']
        tramos.append((r2(prof), r2(hasta), lito, a1, i1, a2, i2, mins, zona,
                       round(py * random.uniform(0.6, 1.4), 2), round(cu * random.uniform(0.5, 1.6), 2),
                       round(mo * random.uniform(0.5, 1.5), 3)))
        prof = hasta
    return tramos


ql = []
for cod, s in SON.items():
    if not s['ini']:
        continue
    limite = s['lim']['geologico']
    hueco = (210.0, 216.4) if cod == 'DDH-COND-N-004-26' else None
    for i, (a, b, lito, a1, i1, a2, i2, mins, zona, py, cu, mo) in enumerate(columna(s)):
        if a >= limite:
            break
        b = min(b, limite)
        if hueco and a < hueco[1] and b > hueco[0]:
            # Hueco a propósito: tramo sin describir entre 210 y 216.4 m.
            if a < hueco[0]:
                b = hueco[0]
            elif b > hueco[1]:
                a = hueco[1]
            else:
                continue
        if b - a < 0.05:
            continue
        ql.append(dict(id=uid('ql', cod, i), sondaje_id=s['id'], desde=a, hasta=b, longitud=r2(b - a),
                       lito_id=cat('lit_diccionario', lito), alt_tipo_1_id=cat('alt_diccionario', a1),
                       alt_intensidad_1_id=cat('alt_intensidad', i1),
                       alt_tipo_2_id=cat('alt_diccionario', a2) if a2 else None,
                       alt_intensidad_2_id=cat('alt_intensidad', i2) if i2 else None,
                       min_tipo_1_id=cat('min_diccionario', mins[0]),
                       min_tipo_2_id=cat('min_diccionario', mins[1]) if len(mins) > 1 else None,
                       min_tipo_3_id=cat('min_diccionario', mins[2]) if len(mins) > 2 else None,
                       pc_py=py, pc_cu=cu, pc_mo=mo, min_zona_id=cat('min_zona', zona),
                       comentarios=None, fecha=min(s['ini'] + timedelta(days=4 + int(a // 24)), HOY),
                       geologo=EMP[GEOLOGOS[s['n'] % 3]][1]))
insert('logueo.iquicklog', ql)

# ------------------------------------------------------- survey y desvíos
surveys, mediciones = [], []
DECISIONES = ['continuar', 'continuar', 'continuar', 'evaluar', 'corregir']
for cod, s in SON.items():
    if not s['ini'] or s['actual'] < 30:
        continue
    da, di = 0.0, 0.0
    deriva_a, deriva_i = random.uniform(-0.05, 0.08), random.uniform(0.01, 0.05)
    nro = 0
    for prof in range(0, int(s['actual']) + 1, 30):
        da += deriva_a * 30 / 10 + random.gauss(0, 0.15)
        di += deriva_i * 30 / 10 + random.gauss(0, 0.08)
        surveys.append(dict(id=uid('svy', cod, prof), sondaje_id=s['id'], azimut=round(s['az'] + da, 4),
                            inclinacion=round(s['inc'] + di, 4), profundidad=float(prof), medicion_utilizada=None,
                            metodo_medicion='Orientación giroscópica', qc='Pass', medicion_id=None))
        if prof and prof % 60 == 0:
            nro += 1
            desv_m = round(prof * math.sin(math.radians(math.hypot(da, di))), 2)
            decision = 'corregir' if desv_m > 8 else ('evaluar' if desv_m > 4 else 'continuar')
            seguimiento = {'corregir': 'pendiente', 'evaluar': 'en_curso'}.get(decision,
                                                                             'cerrado' if nro % 3 == 0 else 'sin_seguimiento')
            mediciones.append(dict(
                id=uid('med', cod, prof), sondaje_id=s['id'], maquina_id=MAQ[s['maq']]['id'],
                perforista_id=EMP[PERFORISTAS[s['n'] % 4]][0], profundidad=float(prof), nro_medicion=nro,
                fecha_medicion=min(s['ini'] + timedelta(days=int(prof / 22)), HOY), fecha_hora=None,
                responsable=EMP[GEOLOGOS[s['n'] % 3]][1], metodo='Orientación giroscópica', instrumento='Gyro Reflex',
                azimut_medido=round(s['az'] + da, 4), inclinacion_medida=round(s['inc'] + di, 4),
                azimut_plan=float(s['az']), inclinacion_plan=float(s['inc']), desv_azimut=round(da, 4),
                desv_azimut_sentido='derecha' if da >= 0 else 'izquierda', desv_inclinacion=round(di, 4),
                desv_inclinacion_sentido='arriba' if di >= 0 else 'abajo', desv_metros=desv_m, desv_total=desv_m,
                es_planificada=True, decision=decision, observaciones=None,
                interpretacion=('El sondaje levanta al cortar las calizas: el contacto con el skarn desvía la corona.'
                                if decision != 'continuar' else None),
                instruccion=('Reducir empuje y rotación hasta salir del skarn; nueva medición a 30 m.'
                             if decision == 'corregir' else
                             ('Repetir la medición en 30 m antes de decidir.' if decision == 'evaluar' else None)),
                responsable_seguimiento=EMP['GEO-MROJ'][1] if decision != 'continuar' else None,
                estado_seguimiento=seguimiento))
insert('operacional.prf_survey', surveys)
insert('operacional.prf_survey_medicion', mediciones)

# ------------------------------------------------------------------ salida
cabecera = [
    '-- Campaña ficticia CÓNDOR para boulder_demo. Generado por generar_demo.py; no editar a mano.',
    'BEGIN;',
    'SET session_replication_role = replica;  -- sin disparadores de auditoría durante la carga',
    '-- Se puede volver a correr: primero se borra la campaña anterior (las fotos se',
    '-- cargan aparte con cargar_fotos.py). Usuarios, roles, cargos y catálogos quedan.',
    'DELETE FROM operacional.fot_foto;',
    'DELETE FROM operacional.prf_avance_personal;',
    'DELETE FROM operacional.prf_avance;',
    'DELETE FROM operacional.log_recepcion_caja;',
    'DELETE FROM operacional.log_avance;',
    'DELETE FROM operacional.fot_avance;',
    'DELETE FROM operacional.crt_muestra;',
    'DELETE FROM operacional.mst_avance;',
    'DELETE FROM operacional.crt_densidad;',
    'DELETE FROM operacional.dns_avance;',
    'DELETE FROM operacional.prf_survey_medicion;',
    'DELETE FROM operacional.prf_survey;',
    'DELETE FROM logueo.iquicklog;',
    'DELETE FROM operacional.prf_sondaje;',
    'DELETE FROM operacional.prf_collar;',
    'DELETE FROM operacional.prf_programa_sondaje;',
    'DELETE FROM operacional.prf_proyecto_maquina;',
    'DELETE FROM operacional.prf_plataforma;',
    'DELETE FROM operacional.prf_maquina;',
    'DELETE FROM base.empleado;',
    'DELETE FROM base.proyecto;',
    'DELETE FROM base.empresa;',
]
sondajes_sql = []
cols = list(sondajes[0].keys())
sondajes_sql.append(f'INSERT INTO operacional.prf_sondaje ({", ".join(cols)}) VALUES')
sondajes_sql.append(',\n'.join('(' + ', '.join(q(f[c]) for c in cols) + ')' for f in sondajes) + ';')
# Los sondajes después de collares y programas, antes de los partes.
indice = next(i for i, l in enumerate(SQL) if l.startswith('INSERT INTO operacional.prf_avance '))
cuerpo = SQL[:indice] + sondajes_sql + SQL[indice:]
pie = ['SET session_replication_role = origin;', 'COMMIT;']
Path(__file__).with_name('datos_demo.sql').write_text('\n'.join(cabecera + cuerpo + pie) + '\n', encoding='utf-8')

print(f'sondajes {len(sondajes)} · partes perforación {len(avances)} · quicklog {len(ql)} · '
      f'survey {len(surveys)} · mediciones {len(mediciones)}')
for cod, s in SON.items():
    lim = s.get('lim', {})
    print(cod, s['actual'], {k: v for k, v in lim.items()})
