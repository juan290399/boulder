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

## Herramientas

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
