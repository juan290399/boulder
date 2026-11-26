# Base de demostración (boulder_demo)

Campaña **ficticia** «Cóndor» para la evaluación del Curso Integrador I. La base
real (`boulder_db`, campaña VERONICA) no se toca: la aplicación apunta a una u
otra desde la configuración local.

## Qué contiene

- Empresa minera y contratista de perforación, proyecto CND (UTM 18S), 6
  plataformas, 3 máquinas y 17 empleados.
- 10 sondajes `DDH-COND-N-001-26` … `010`: seis terminados, tres en
  perforación y uno programado.
- Partes diarios de perforación por turno con su personal, y las etapas que
  siguen (recepción, logueo geológico/geotécnico/estructural, fotografía,
  corte, muestreo, densidad) con atrasos: en Avances se ve dónde se corta la
  cadena en los sondajes recientes.
- Quicklog continuo (pórfido de Cu-Mo con skarn); en `DDH-COND-N-004-26` hay
  un hueco a propósito entre 210 y 216,4 m.
- Estaciones de survey cada 30 m y mediciones de desvío cada 60 m con
  decisiones (continuar, evaluar, corregir) y su seguimiento.
- 312 fotos reales de cajas (VERONICA) reasignadas a los sondajes ficticios,
  en pares seco/húmedo.

Usuarios, roles, cargos y diccionarios geológicos se copian de `boulder_db`.

## Armarla desde cero

```powershell
createdb -U postgres -T template0 -E UTF8 boulder_demo
pg_dump -U postgres -s boulder_db | psql -U postgres -d boulder_demo
pg_dump -U postgres -a -n geologia boulder_db | psql -U postgres -d boulder_demo
pg_dump -U postgres -a -t base.rol -t base.cargo -t base.usuario -t base.usuario_rol boulder_db | psql -U postgres -d boulder_demo
psql -U postgres -d boulder_demo -c "update base.usuario set empleado_id = null"
python generar_demo.py
psql -U postgres -d boulder_demo -f datos_demo.sql
python cargar_fotos.py F:\02_archivo_proyectos\VERONICA\perforacion\fotos_ddh ..\..\datos\fotos-demo > fotos_demo.sql
psql -U postgres -d boulder_demo -f fotos_demo.sql
```

`datos_demo.sql` se puede volver a correr: borra la campaña anterior antes de
cargar (usuarios y catálogos quedan). Después hay que volver a cargar las fotos.

## Usarla

En `src/main/resources/application-local.properties` (ignorado por Git):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/boulder_demo
boulder.fotos.directorio=./datos/fotos-demo
```

Para volver a la base real se borran esas dos líneas y se reinicia el servidor.
