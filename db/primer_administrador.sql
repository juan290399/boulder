-- ---------------------------------------------------------------------------
-- Activa la primera cuenta y le da el rol ADMIN.
--
-- La tabla base.usuario nace vacía tras migracion_campamento.sql. La persona se
-- registra primero desde la pantalla de inicio de sesión (la cuenta queda
-- inactiva y sin roles, y su contraseña cifrada con BCrypt); luego se corre
-- este script con su nombre de usuario. Las cuentas siguientes las activa ese
-- administrador desde la aplicación.
--
-- Uso:
--   psql -U postgres -d VERONICA -v ON_ERROR_STOP=1 -v usuario=jvelazco -f primer_administrador.sql
-- ---------------------------------------------------------------------------

BEGIN;

UPDATE base.usuario SET estado = true WHERE usuario = :'usuario';

INSERT INTO base.usuario_rol (id_usuario, id_rol, asignado_el)
SELECT u.id, r.id, now()
FROM base.usuario u CROSS JOIN base.rol r
WHERE u.usuario = :'usuario' AND r.codigo = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Debe devolver una fila con estado = t y rol = ADMIN; si no devuelve nada, el
-- usuario no existe todavía.
SELECT u.usuario, u.estado, r.codigo AS rol
FROM base.usuario u
JOIN base.usuario_rol ur ON ur.id_usuario = u.id
JOIN base.rol r ON r.id = ur.id_rol
WHERE u.usuario = :'usuario';

COMMIT;
