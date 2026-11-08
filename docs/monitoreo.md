# Plan de monitoreo

## Qué se vigila

| Aspecto | Fuente | Umbral de alerta | Frecuencia |
|---|---|---|---|
| Disponibilidad | `GET /actuator/health` | distinto de `UP` o sin respuesta en 10 s | cada 5 min (`verificar_salud.sh`) |
| Tiempo de respuesta de la salud | `verificar_salud.sh` (ms) | > 2000 ms tres veces seguidas | cada 5 min |
| Base de datos | componente `db` de la salud | `DOWN` | cada 5 min |
| Disco | componente `diskSpace` y `df` | < 10 % libre | cada 5 min |
| Memoria de la JVM | `/actuator/metrics/jvm.memory.used` | > 85 % del máximo | diaria o ante lentitud |
| CPU del proceso | `/actuator/metrics/process.cpu.usage` | > 80 % sostenido 15 min | diaria o ante lentitud |
| Conexiones a la base | `/actuator/metrics/hikaricp.connections.active` | = tamaño del pool | diaria |
| Latencia del API | `/actuator/metrics/http.server.requests` | p95 > 1 s | semanal |
| Errores | `boulder.log` (nivel ERROR) | cualquier ERROR nuevo | diaria |
| Seguridad | `seguridad.log` (bloqueos, accesos denegados) | > 20 bloqueos en un día | diaria |
| Sincronización de GeoLog | `GET /api/logueo/dispositivos` | tablet sin contacto > 24 h en campaña | diaria |
| Respaldos | `respaldos.log` | línea `ERROR` o sin respaldo del día | diaria |
| Almacén de fotos | componente `almacenFotos` de la salud | `DOWN` (no escribible o < 1 GB libre) | cada 5 min |

## Métricas del negocio

Las métricas de la JVM dicen si el proceso está sano; estas dicen si el sistema
hace su trabajo. Se definen en `MetricasNegocio` y `TareasMantenimiento` y se
publican con las demás en `/actuator/metrics` y `/actuator/prometheus`.

| Métrica | Tipo | Etiquetas | Qué indica | Alerta (`deploy/monitoreo/alertas.yml`) |
|---|---|---|---|---|
| `boulder.datos.filas` | contador | `recurso`, `origen` (tablet, web, importacion) | Filas confirmadas por cada canal de entrada | `SinPartesEnTurno`: ningún parte de avance en 12 h |
| `boulder.logueo.rechazadas` | contador | `plantilla` | Filas de GeoLog que la base rechazó al sincronizar | `RechazosLogueo`: > 20 en una hora |
| `boulder.login` | contador | `resultado` (correcto, fallido, bloqueado) | Inicios de sesión | `FuerzaBrutaLogin`: > 20 bloqueos en un día |
| `boulder.geolog.dispositivos.sin_contacto` | indicador | — | Tablets activas en la semana sin sincronizar hace más de 24 h | `TabletSinSincronizar` |
| `http.server.requests` | histograma | `uri`, `status` | Latencia y errores del API | `LatenciaApiAlta` (p95 > 1 s), `ErroresServidor` |

## Bitácoras

Cada línea lleva nivel (`ERROR` para fallos no controlados, `WARN` para
rechazos y situaciones recuperables, `INFO` para hechos del negocio, `DEBUG`
solo en desarrollo) y el **id de la petición** (`X-Request-Id`), que Nginx genera
y la aplicación devuelve en la respuesta. Un usuario que reporta un error da ese
id y se encuentran todas las líneas de su petición.

| Archivo | Formato | Retención | Uso |
|---|---|---|---|
| `boulder.log` | texto, una línea por evento | 30 días | lectura a ojo |
| `boulder.json` | JSON (ECS), una línea por evento | 30 días | herramientas: `jq`, Filebeat, Loki |
| `seguridad.log` | texto | 90 días | auditoría de inicios de sesión |
| `monitoreo.log` | texto | — | resultado de `verificar_salud.sh` |

## Herramientas

- **Prometheus** (`deploy/monitoreo/prometheus.yml`): raspa
  `/actuator/prometheus` cada 30 s desde el mismo servidor (la aplicación solo
  acepta esa ruta desde `127.0.0.1` y Nginx la niega desde fuera) y evalúa las
  reglas de `deploy/monitoreo/alertas.yml`: salud, rendimiento y negocio.
- **Spring Boot Actuator** (`/actuator/health` público con solo el estado;
  detalle, `info` y métricas para ADMIN). Las métricas son de Micrometer.
- **Logback**: `boulder.log` (30 días) y `seguridad.log` (90 días), con rotación
  diaria; la ruta se fija con `BOULDER_LOGS`.
- **Scripts**: `scripts/mantenimiento/verificar_salud.sh` escribe cada chequeo en
  `logs/monitoreo.log` y sale con código 1 si la aplicación no está `UP`, para que
  cron o el Programador de tareas lo registren como fallo.
- **Sistema operativo**: `journalctl -u boulder`, `df -h`, `free -m`, `top`; en
  Windows, el Visor de eventos y el Monitor de recursos.

## Consultas útiles

```bash
# Salud con detalle (token de ADMIN)
curl -s -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/actuator/health

# Memoria usada y peticiones HTTP
curl -s -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/actuator/metrics/jvm.memory.used
curl -s -H "Authorization: Bearer $TOKEN" "http://127.0.0.1:8080/actuator/metrics/http.server.requests?tag=status:500"

# Caídas registradas hoy
grep "$(date +%F)" logs/monitoreo.log | grep CAIDA

# Todas las líneas de una petición, por su id
jq -c 'select(.peticion == "4f2a9c1b-7d3e")' /var/log/boulder/boulder.json

# Errores de hoy en el log estructurado
jq -c 'select(."log.level" == "ERROR")' /var/log/boulder/boulder.json

# Métricas del negocio en formato Prometheus (desde el servidor)
curl -s http://127.0.0.1:8080/actuator/prometheus | grep '^boulder_'

# Bloqueos por intentos fallidos
grep -c "bloqueada" /var/log/boulder/seguridad.log
```

## Respuesta ante una alerta

1. **Caída**: `systemctl status boulder` y `journalctl -u boulder -n 100`. Si la
   causa es la base, `systemctl status postgresql`. Reiniciar con
   `systemctl restart boulder` y confirmar con `verificar_salud.sh`.
2. **Disco lleno**: revisar `/var/backups/boulder` (retención de
   `RETENCION_DIAS`) y `/var/lib/boulder/fotos`; las bitácoras ya rotan solas.
3. **Lentitud**: memoria y CPU en Actuator; conexiones activas del pool; consultas
   lentas en `pg_stat_activity`.
4. **Ataque de fuerza bruta**: los bloqueos aparecen en `seguridad.log` con usuario
   e IP; si persisten desde una IP, bloquearla en Nginx o en el firewall.
