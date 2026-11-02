# Reporte de pruebas de seguridad

## Alcance y método

- **Pruebas automatizadas** contra la cadena de filtros real (87 pruebas en 10
  clases): autenticación, autorización por rol, tokens manipulados, inyección,
  cabeceras, fuga de información, carga de archivos y monitoreo.
- **Auditoría de dependencias**: `npm audit` del frontend y revisión de las
  versiones del backend contra avisos públicos (CVE).
- **Revisión manual** de configuración: credenciales, CORS, secretos y despliegue.

Referencia: OWASP Top 10 (2021).

## Pruebas automatizadas

| OWASP | Ataque o control | Resultado esperado | Prueba |
|---|---|---|---|
| A01 Control de acceso | Escritura con rol VISITANTE | 403 | `PermisosApiIT`, `PermisosEscrituraTest` |
| A01 | Borrar un parte con rol ASISTENTE_GEOLOGIA | 403 | `PermisosApiIT` |
| A01 | Administración de cuentas sin rol ADMIN | 403 | `SeguridadAtaquesIT` |
| A01 | Métricas de Actuator sin ADMIN | 403 | `MonitoreoIT` |
| A02 Fallas criptográficas | Contraseñas guardadas con BCrypt | hash `{bcrypt}` | `CambioContrasenaServiceTest` |
| A03 Inyección | `' OR '1'='1`, `; DROP TABLE` en parámetros | 200 vacío, tabla intacta | `SeguridadAtaquesIT` |
| A03 | Fórmula `=…` en un código exportado a Excel | celda de texto | `ReporteDiarioExcelTest` y otros |
| A03 | Ruta `../../etc/passwd` en el nombre de una foto | nombre saneado | `NombreArchivoSeguroTest`, `AlmacenFotosDiscoTest` |
| A04 Diseño inseguro | Auto-registro con `"roles":["ADMIN"],"estado":true` | cuenta inactiva y sin roles | `SeguridadAtaquesIT` |
| A05 Configuración | Cabeceras `X-Content-Type-Options`, `X-Frame-Options`, `Cache-Control` | presentes | `SeguridadAtaquesIT` |
| A05 | Origen ajeno en CORS | 403 | `SeguridadApiTest` |
| A05 | JSON mal formado | 400 sin trazas ni nombres de clases | `SeguridadAtaquesIT` |
| A07 Autenticación | Sexto intento fallido seguido | 429, cuenta bloqueada 15 min | `SeguridadApiTest`, `LimiteIntentosLoginTest` |
| A07 | Token con `alg: none` | 403 | `SeguridadAtaquesIT` |
| A07 | Token firmado con otra clave | 403 | `SeguridadAtaquesIT` |
| A07 | Token vencido con firma válida | 403 | `SeguridadAtaquesIT` |
| A07 | Roles del token alterados a mano | 403 | `SeguridadAtaquesIT` |
| A07 | Token vigente de una cuenta desactivada o borrada | 403 | `SeguridadApiTest`, `CuentasActivasTest` |
| A09 Registro | Intentos de inicio de sesión en `seguridad.log`, sin contraseña y sin saltos de línea inyectados | registrados | revisión de la bitácora tras `SeguridadApiTest` |

## Hallazgos

| # | Hallazgo | Severidad | Estado |
|---|---|---|---|
| 1 | Driver JDBC de PostgreSQL 42.6.0 con CVE-2024-1597 (inyección SQL en modo `simple`) | Alta | Corregido: 42.7.10, versión gestionada por Spring Boot (`5a59554`) |
| 2 | Angular 21.2.14 con 6 avisos en dependencias de producción (4 altos, 2 moderados) | Alta | Corregido: 21.2.25; `npm audit --omit=dev` sin hallazgos (`744cf8e`) |
| 3 | Contraseña de la base escrita en scripts de prueba versionados | Alta | Corregido: se lee de variable o archivo local; contraseña rotada e historial depurado (`60ba592`) |
| 4 | `JWT_SECRET` local de 13 caracteres, por debajo de los 256 bits de HMAC-SHA256 | Media | Corregido: clave aleatoria de 64 caracteres; el ejemplo de despliegue exige 32 o más |
| 5 | JSON mal formado respondía 500 | Baja | Corregido: 400 con mensaje genérico (`7364458`) |
| 6 | 16 avisos moderados en herramientas de desarrollo (cadena de `@angular/cli`) | Baja | Aceptado: no llegan al navegador ni al servidor |
| 7 | Una violación de restricción devuelve el mensaje de PostgreSQL (nombres de restricciones) | Baja | Pendiente: reemplazar por un mensaje propio por restricción |
| 8 | El token vive en `localStorage`; un XSS podría leerlo | Baja | Mitigado: Angular escapa las plantillas; recomendación: cookie `HttpOnly` |

## Configuración revisada

- Credenciales y clave JWT fuera del repositorio (`application-local.properties`
  ignorado; variables de entorno en producción).
- HTTPS en el proxy Nginx con HSTS; `/actuator/metrics` bloqueado desde fuera
  ([deploy/nginx-boulder.conf](../deploy/nginx-boulder.conf)).
- Servicio systemd sin privilegios nuevos y con escritura solo en sus carpetas
  ([deploy/boulder.service](../deploy/boulder.service)).
