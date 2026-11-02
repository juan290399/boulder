# Despliegue

Un solo artefacto: el jar de Spring Boot sirve el API y la aplicación Angular
(`classpath:/static`, con retorno a `index.html` para las rutas del cliente).

```
Navegador ──HTTPS──▶ Nginx (443) ──▶ Spring Boot (127.0.0.1:8080) ──▶ PostgreSQL (5432)
                                        │
                                        ├─ /var/lib/boulder/fotos
                                        └─ /var/log/boulder
```

## 1. Construir

Requisitos del equipo de construcción: JDK 21 y Node.js 20 o superior. Maven no
hace falta: el wrapper descarga Maven 3.9.9 y verifica su SHA-256.

```bash
./mvnw -Pproduccion -DskipTests package
```

El perfil `produccion` ejecuta `npm ci` y `ng build` en `boulder-app/` y copia el
resultado a `target/classes/static`. Sale `target/boulder-drilling.jar` (~90 MB).

## 2. Preparar el servidor (Linux)

```bash
sudo useradd --system --home /opt/boulder boulder
sudo mkdir -p /opt/boulder /etc/boulder /var/lib/boulder/fotos /var/log/boulder /var/backups/boulder
sudo chown -R boulder: /opt/boulder /var/lib/boulder /var/log/boulder /var/backups/boulder

# Base de datos
sudo -u postgres createuser --pwprompt boulder
sudo -u postgres createdb -O boulder boulder_db
psql -U boulder -d boulder_db -f db/boulder_db_esquema_fisico.sql
```

## 3. Instalar

```bash
sudo cp target/boulder-drilling.jar /opt/boulder/
sudo cp -r scripts /opt/boulder/
sudo cp deploy/boulder.env.example /etc/boulder/boulder.env
sudo chmod 600 /etc/boulder/boulder.env      # editar contraseña y JWT_SECRET
sudo cp deploy/boulder.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now boulder
```

Proxy con HTTPS:

```bash
sudo cp deploy/nginx-boulder.conf /etc/nginx/sites-available/boulder
sudo ln -s /etc/nginx/sites-available/boulder /etc/nginx/sites-enabled/
sudo certbot --nginx -d boulder.ejemplo.pe
sudo nginx -t && sudo systemctl reload nginx
```

## 4. Verificar

```bash
curl -s http://127.0.0.1:8080/actuator/health        # {"status":"UP",...}
scripts/mantenimiento/verificar_salud.sh             # deja constancia en logs/monitoreo.log
journalctl -u boulder -n 50
```

## Servidor Windows (campamento)

Copiar el jar y `deploy/` a `C:\boulder`, crear `deploy\boulder.env` a partir del
ejemplo y arrancar con `deploy\iniciar.ps1`. Para que arranque con el equipo, una
tarea programada con el desencadenador «Al iniciar». El respaldo y la vigilancia
se programan con `scripts\mantenimiento\programar_tareas.ps1`.

## Variables de entorno

| Variable | Uso | Valor por defecto |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Activa `application-prod.properties` | — (usar `prod`) |
| `PORT` | Puerto HTTP | 8080 |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL | `localhost:5432/boulder_db`, `postgres` |
| `DB_POOL` | Conexiones del pool Hikari | 10 |
| `JWT_SECRET` | Clave HMAC-SHA256 del token (≥ 32 caracteres) | — |
| `BOULDER_FOTOS` | Almacén de fotos de testigo | `/var/lib/boulder/fotos` |
| `BOULDER_LOGS` | Carpeta de bitácoras | `/var/log/boulder` |

## Actualizar a una versión nueva

```bash
scripts/mantenimiento/respaldo_bd.sh /var/backups/boulder   # respaldo antes de tocar nada
sudo cp target/boulder-drilling.jar /opt/boulder/
sudo systemctl restart boulder
curl -s http://127.0.0.1:8080/actuator/health
```

Si la versión nueva falla, se vuelve al jar anterior y, si hubo cambios en la base,
se restaura el respaldo con `scripts/mantenimiento/restaurar_bd.sh`.
