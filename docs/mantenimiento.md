# Plan de mantenimiento

## Tareas programadas

| Tarea | Script | Programación | Retención |
|---|---|---|---|
| Respaldo de la base (`pg_dump -Fc`) | `scripts/mantenimiento/respaldo_bd.sh` | diaria, 2:00 | 14 días (`RETENCION_DIAS`) |
| Verificación de salud | `scripts/mantenimiento/verificar_salud.sh` | cada 5 min | `logs/monitoreo.log` |
| Copia del respaldo fuera del servidor | `rsync` (ver `crontab.txt`) | semanal, domingo 3:00 | según el almacén |
| Estadísticas del planificador | `VACUUM (ANALYZE)` | semanal, domingo 4:00 | — |
| Rotación de bitácoras | Logback | diaria, automática | 30 días general y JSON, 90 seguridad |

En Linux se instalan con `crontab -e` a partir de
[scripts/mantenimiento/crontab.txt](../scripts/mantenimiento/crontab.txt); en
Windows, con `scripts\mantenimiento\programar_tareas.ps1`, que registra las tareas
en el Programador de tareas.

### Tareas de la propia aplicación

Además de las del sistema operativo, la aplicación corre dos tareas con
expresiones cron de Spring (`TareasMantenimiento`). Limpian y revisan lo que
solo ella conoce, y el horario se cambia sin recompilar con una variable de
entorno:

| Tarea | Programación por defecto (hora de Lima) | Propiedad | Qué hace |
|---|---|---|---|
| Purga de subidas a medias | `0 30 3 * * *` (diaria, 3:30) | `boulder.mantenimiento.cron.temporales` | Borra los `.subida-*.tmp` de más de 24 h que deja una subida de fotos cortada |
| Revisión de tablets | `0 */15 * * * *` (cada 15 min) | `boulder.mantenimiento.cron.dispositivos` | Cuenta las tablets de GeoLog sin sincronizar hace más de 24 h y publica la métrica `boulder.geolog.dispositivos.sin_contacto` |

Cada corrida deja una línea `INFO` (o `WARN` si encuentra algo) en `boulder.log`
con el prefijo `Mantenimiento:`. Las pruebas `TareasMantenimientoTest` fijan el
reloj y comprueban qué se borra y qué se conserva.

## Respaldos

```bash
scripts/mantenimiento/respaldo_bd.sh /var/backups/boulder
```

- Formato custom de `pg_dump`, comprimido; ~2 MB con los datos actuales.
- Cada ejecución deja una línea `OK` o `ERROR` en `respaldos.log`.
- Las fotos de testigo (`BOULDER_FOTOS`) se respaldan con la copia semanal de la
  carpeta; no cambian una vez subidas.

### Restauración

```bash
# En una base nueva, para revisar sin tocar la productiva
scripts/mantenimiento/restaurar_bd.sh /var/backups/boulder/boulder_db_20261102_020000.dump boulder_db_revision

# Sobre la productiva (detener el servicio antes)
sudo systemctl stop boulder
scripts/mantenimiento/restaurar_bd.sh archivo.dump boulder_db --reemplazar
sudo systemctl start boulder
```

El script valida que el archivo sea un respaldo y no sobrescribe una base que ya
existe sin `--reemplazar`.

**Prueba de restauración realizada**: respaldo de `boulder_db` restaurado en una
base temporal con el mismo contenido (70 tablas, 72 sondajes, 2160 tramos de
quicklog); la base temporal se eliminó después.

## Mantenimiento correctivo y evolutivo

1. Todo cambio entra por una rama de rol (`backend`, `frontend`, `docs-qa`) y se
   integra a `develop` con `--no-ff`; `main` recibe solo entregas etiquetadas.
2. Cada corrección lleva primero la prueba que reproduce el fallo (TDD).
3. Antes de desplegar: `./mvnw verify` en verde, respaldo de la base, despliegue,
   verificación de salud (ver [despliegue.md](despliegue.md)).

## Actualización de dependencias

| Qué | Frecuencia | Cómo |
|---|---|---|
| Frontend | mensual | `npm audit --omit=dev`; parches con `npm install @angular/…@^21.x` |
| Backend | mensual | versión de Spring Boot en `pom.xml` y `build.gradle.kts`; revisar avisos de las librerías fijadas (POI, Guava, Commons, JJWT) |
| JDK y PostgreSQL | según avisos de seguridad | actualización menor del sistema operativo |

## Cambios de esquema

El esquema físico está en `db/boulder_db_esquema_fisico.sql` y Hibernate solo lo
valida (`ddl-auto=validate`): un cambio de tabla se aplica con un script SQL
versionado, después de un respaldo, y luego se despliega el jar que lo usa.
