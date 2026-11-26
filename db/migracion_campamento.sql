-- ---------------------------------------------------------------------------
-- Lleva la base VERONICA del campamento al esquema que necesita Boulder.
--
-- Solo agrega: tablas, columnas, índices, disparadores y vistas que faltan en
-- los esquemas base, logueo y operacional. No borra ni modifica datos, y no toca
-- el esquema laboratorio (geoquimica, peso_lab, peso_muestreo) ni
-- operacional.datos, vw_sondaje y vw_mm_collar.
--
-- En base.empresa, codigo y ruc pasan de char a varchar (mismo valor, sin el
-- relleno de espacios).
--
-- Lo único que se reemplaza son las restricciones de no solapamiento de las
-- cinco tablas de logueo (pasan a ignorar las filas con borrado lógico) y las
-- vistas logueo.vw_mm_*, que ganan el filtro «eliminado_en IS NULL».
--
-- Corre en una sola transacción: si algo falla, no queda nada aplicado.
-- Antes: respaldo con pg_dump. Uso:
--   psql -U postgres -d VERONICA -v ON_ERROR_STOP=1 -f migracion_campamento.sql
-- ---------------------------------------------------------------------------

BEGIN;

-- ===== 1. Sincronización del logueo: borrado lógico y marca de cambio =====
-- ---------------------------------------------------------------------------
-- Sincronización del logueo entre Boulder GeoLog y Boulder Drilling.
--
-- 1. eliminado_en: borrado lógico. Cuando el geólogo borra un tramo en el
--    tablet, la fila no desaparece: se marca. Así la marca viaja al servidor y
--    de ahí a los demás tablets. Con el borrado físico que había, el servidor
--    nunca se enteraba y la fila reaparecía en la siguiente sincronización,
--    porque la bajada volvía a traer lo que el tablet había quitado.
--
-- 2. actualizado_en: cuándo cambió la fila en el servidor. Lo pone un trigger
--    y no la aplicación, para que valga igual para el tablet, la web y el
--    importador. Es la base para sincronizar solo lo cambiado y para avisar en
--    tiempo real.
--
-- 3. Las vistas vw_mm_* (exportación a Micromine) dejan fuera lo eliminado.
--
-- Idempotente: se puede volver a correr.
-- ---------------------------------------------------------------------------


CREATE OR REPLACE FUNCTION logueo.tocar_actualizado_en() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    NEW.actualizado_en := now();
    RETURN NEW;
END;
$$;


ALTER TABLE logueo.ilitologia ADD COLUMN IF NOT EXISTS eliminado_en   timestamptz;
ALTER TABLE logueo.ilitologia ADD COLUMN IF NOT EXISTS actualizado_en timestamptz NOT NULL DEFAULT now();
COMMENT ON COLUMN logueo.ilitologia.eliminado_en   IS 'Borrado lógico: fecha en que se eliminó. NULL = vigente.';
COMMENT ON COLUMN logueo.ilitologia.actualizado_en IS 'Último cambio en el servidor. Lo mantiene el trigger ilitologia_actualizado_en.';
DROP TRIGGER IF EXISTS ilitologia_actualizado_en ON logueo.ilitologia;
CREATE TRIGGER ilitologia_actualizado_en BEFORE INSERT OR UPDATE ON logueo.ilitologia
    FOR EACH ROW EXECUTE FUNCTION logueo.tocar_actualizado_en();
CREATE INDEX IF NOT EXISTS ilitologia_sondaje_actualizado_idx ON logueo.ilitologia (sondaje_id, actualizado_en);

ALTER TABLE logueo.ialteracion ADD COLUMN IF NOT EXISTS eliminado_en   timestamptz;
ALTER TABLE logueo.ialteracion ADD COLUMN IF NOT EXISTS actualizado_en timestamptz NOT NULL DEFAULT now();
COMMENT ON COLUMN logueo.ialteracion.eliminado_en   IS 'Borrado lógico: fecha en que se eliminó. NULL = vigente.';
COMMENT ON COLUMN logueo.ialteracion.actualizado_en IS 'Último cambio en el servidor. Lo mantiene el trigger ialteracion_actualizado_en.';
DROP TRIGGER IF EXISTS ialteracion_actualizado_en ON logueo.ialteracion;
CREATE TRIGGER ialteracion_actualizado_en BEFORE INSERT OR UPDATE ON logueo.ialteracion
    FOR EACH ROW EXECUTE FUNCTION logueo.tocar_actualizado_en();
CREATE INDEX IF NOT EXISTS ialteracion_sondaje_actualizado_idx ON logueo.ialteracion (sondaje_id, actualizado_en);

ALTER TABLE logueo.imineralizacion ADD COLUMN IF NOT EXISTS eliminado_en   timestamptz;
ALTER TABLE logueo.imineralizacion ADD COLUMN IF NOT EXISTS actualizado_en timestamptz NOT NULL DEFAULT now();
COMMENT ON COLUMN logueo.imineralizacion.eliminado_en   IS 'Borrado lógico: fecha en que se eliminó. NULL = vigente.';
COMMENT ON COLUMN logueo.imineralizacion.actualizado_en IS 'Último cambio en el servidor. Lo mantiene el trigger imineralizacion_actualizado_en.';
DROP TRIGGER IF EXISTS imineralizacion_actualizado_en ON logueo.imineralizacion;
CREATE TRIGGER imineralizacion_actualizado_en BEFORE INSERT OR UPDATE ON logueo.imineralizacion
    FOR EACH ROW EXECUTE FUNCTION logueo.tocar_actualizado_en();
CREATE INDEX IF NOT EXISTS imineralizacion_sondaje_actualizado_idx ON logueo.imineralizacion (sondaje_id, actualizado_en);

ALTER TABLE logueo.iestructural ADD COLUMN IF NOT EXISTS eliminado_en   timestamptz;
ALTER TABLE logueo.iestructural ADD COLUMN IF NOT EXISTS actualizado_en timestamptz NOT NULL DEFAULT now();
COMMENT ON COLUMN logueo.iestructural.eliminado_en   IS 'Borrado lógico: fecha en que se eliminó. NULL = vigente.';
COMMENT ON COLUMN logueo.iestructural.actualizado_en IS 'Último cambio en el servidor. Lo mantiene el trigger iestructural_actualizado_en.';
DROP TRIGGER IF EXISTS iestructural_actualizado_en ON logueo.iestructural;
CREATE TRIGGER iestructural_actualizado_en BEFORE INSERT OR UPDATE ON logueo.iestructural
    FOR EACH ROW EXECUTE FUNCTION logueo.tocar_actualizado_en();
CREATE INDEX IF NOT EXISTS iestructural_sondaje_actualizado_idx ON logueo.iestructural (sondaje_id, actualizado_en);

ALTER TABLE logueo.iquicklog ADD COLUMN IF NOT EXISTS eliminado_en   timestamptz;
ALTER TABLE logueo.iquicklog ADD COLUMN IF NOT EXISTS actualizado_en timestamptz NOT NULL DEFAULT now();
COMMENT ON COLUMN logueo.iquicklog.eliminado_en   IS 'Borrado lógico: fecha en que se eliminó. NULL = vigente.';
COMMENT ON COLUMN logueo.iquicklog.actualizado_en IS 'Último cambio en el servidor. Lo mantiene el trigger iquicklog_actualizado_en.';
DROP TRIGGER IF EXISTS iquicklog_actualizado_en ON logueo.iquicklog;
CREATE TRIGGER iquicklog_actualizado_en BEFORE INSERT OR UPDATE ON logueo.iquicklog
    FOR EACH ROW EXECUTE FUNCTION logueo.tocar_actualizado_en();
CREATE INDEX IF NOT EXISTS iquicklog_sondaje_actualizado_idx ON logueo.iquicklog (sondaje_id, actualizado_en);

CREATE OR REPLACE VIEW logueo.vw_mm_litologia AS
SELECT lit.id,
    s.codigo AS sondaje_codigo,
    s.nombre AS sondaje_nombre,
    lit.desde,
    lit.hasta,
    lit.longitud,
    tm.codigo AS tipo_material,
    c.codigo AS color,
    ic.codigo AS intensidad_color,
    gl.codigo AS grupo_litologico,
    ld.codigo AS tipo_litologia,
    lit.tipo_subroca,
    lit.descripcion_roca,
    tg.codigo AS tamanio_grano,
    txt.codigo AS textura,
    lit.bx_pc_clasto,
    fc.codigo AS bx_forma,
    lit.bx_pc_matriz,
    tgm.codigo AS bx_tipo_matriz,
    lit.comentarios,
    lit.fecha,
    lit.geologo
   FROM logueo.ilitologia lit
     JOIN operacional.prf_sondaje s ON s.id = lit.sondaje_id
     LEFT JOIN geologia.tipo_material tm ON tm.id = lit.tipo_material_id
     LEFT JOIN geologia.color c ON c.id = lit.color_id
     LEFT JOIN geologia.intensidad_color ic ON ic.id = lit.intensidad_color_id
     LEFT JOIN geologia.grupo_litologico gl ON gl.id = lit.grupo_litologico_id
     LEFT JOIN geologia.lit_diccionario ld ON ld.id = lit.lit_diccionario_id
     LEFT JOIN geologia.tamanio_grano tg ON tg.id = lit.tamanio_grano_id
     LEFT JOIN geologia.lit_textura txt ON txt.id = lit.lit_textura_id
     LEFT JOIN geologia.forma_clasto fc ON fc.id = lit.bx_forma_id
     LEFT JOIN geologia.tamanio_grano tgm ON tgm.id = lit.bx_tipo_matriz_id
  WHERE lit.eliminado_en IS NULL
  ORDER BY s.codigo, lit.desde;

CREATE OR REPLACE VIEW logueo.vw_mm_alteracion AS
SELECT alt.id,
    s.codigo AS sondaje_codigo,
    s.nombre AS sondaje_nombre,
    alt.desde,
    alt.hasta,
    alt.longitud,
    am1.codigo AS alt_min_1,
    ae1.codigo AS alt_estilo_1,
    alt.alt_pc_1,
    alt.vetillas_m_1,
    am2.codigo AS alt_min_2,
    ae2.codigo AS alt_estilo_2,
    alt.alt_pc_2,
    alt.vetillas_m_2,
    am3.codigo AS alt_min_3,
    ae3.codigo AS alt_estilo_3,
    alt.alt_pc_3,
    alt.vetillas_m_3,
    am4.codigo AS alt_min_4,
    ae4.codigo AS alt_estilo_4,
    alt.alt_pc_4,
    alt.vetillas_m_4,
    am5.codigo AS alt_min_5,
    ae5.codigo AS alt_estilo_5,
    alt.alt_pc_5,
    alt.vetillas_m_5,
    ad1.codigo AS alt_tipo_1,
    ai1.codigo AS alt_intensidad_1,
    ad2.codigo AS alt_tipo_2,
    ai2.codigo AS alt_intensidad_2,
    alt.comentarios,
    alt.fecha,
    alt.geologo
   FROM logueo.ialteracion alt
     JOIN operacional.prf_sondaje s ON s.id = alt.sondaje_id
     LEFT JOIN geologia.alt_mineral am1 ON am1.id = alt.alt_min_1
     LEFT JOIN geologia.alt_estilo ae1 ON ae1.id = alt.alt_estilo_1
     LEFT JOIN geologia.alt_mineral am2 ON am2.id = alt.alt_min_2
     LEFT JOIN geologia.alt_estilo ae2 ON ae2.id = alt.alt_estilo_2
     LEFT JOIN geologia.alt_mineral am3 ON am3.id = alt.alt_min_3
     LEFT JOIN geologia.alt_estilo ae3 ON ae3.id = alt.alt_estilo_3
     LEFT JOIN geologia.alt_mineral am4 ON am4.id = alt.alt_min_4
     LEFT JOIN geologia.alt_estilo ae4 ON ae4.id = alt.alt_estilo_4
     LEFT JOIN geologia.alt_mineral am5 ON am5.id = alt.alt_min_5
     LEFT JOIN geologia.alt_estilo ae5 ON ae5.id = alt.alt_estilo_5
     LEFT JOIN geologia.alt_diccionario ad1 ON ad1.id = alt.alt_tipo_1
     LEFT JOIN geologia.alt_intensidad ai1 ON ai1.id = alt.alt_intensidad_1
     LEFT JOIN geologia.alt_diccionario ad2 ON ad2.id = alt.alt_tipo_2
     LEFT JOIN geologia.alt_intensidad ai2 ON ai2.id = alt.alt_intensidad_2
  WHERE alt.eliminado_en IS NULL
  ORDER BY s.codigo, alt.desde;

CREATE OR REPLACE VIEW logueo.vw_mm_mineralizacion AS
SELECT im.id,
    s.codigo AS sondaje_codigo,
    s.nombre AS sondaje_nombre,
    im.desde,
    im.hasta,
    im.longitud,
    mz.codigo AS zona_mineralizacion,
    md1.codigo AS min_1,
    im.min_pc_1,
    dist1.codigo AS min_dist_1,
    im.cu_m_pc_1,
    im.cu_pc_1,
    im.mo_m_pc_1,
    im.mo_pc_1,
    md2.codigo AS min_2,
    im.min_pc_2,
    dist2.codigo AS min_dist_2,
    im.cu_m_pc_2,
    im.cu_pc_2,
    im.mo_m_pc_2,
    im.mo_pc_2,
    md3.codigo AS min_3,
    im.min_pc_3,
    dist3.codigo AS min_dist_3,
    im.cu_m_pc_3,
    im.cu_pc_3,
    im.mo_m_pc_3,
    im.mo_pc_3,
    md4.codigo AS min_4,
    im.min_pc_4,
    dist4.codigo AS min_dist_4,
    im.cu_m_pc_4,
    im.cu_pc_4,
    im.mo_m_pc_4,
    im.mo_pc_4,
    im.cu_pct,
    im.mo_pct,
    im.descripcion,
    im.fecha,
    im.geologo
   FROM logueo.imineralizacion im
     JOIN operacional.prf_sondaje s ON s.id = im.sondaje_id
     LEFT JOIN geologia.min_zona mz ON mz.id = im.min_zona_id
     LEFT JOIN geologia.min_diccionario md1 ON md1.id = im.min_1
     LEFT JOIN geologia.min_distribucion dist1 ON dist1.id = im.min_dist_1
     LEFT JOIN geologia.min_diccionario md2 ON md2.id = im.min_2
     LEFT JOIN geologia.min_distribucion dist2 ON dist2.id = im.min_dist_2
     LEFT JOIN geologia.min_diccionario md3 ON md3.id = im.min_3
     LEFT JOIN geologia.min_distribucion dist3 ON dist3.id = im.min_dist_3
     LEFT JOIN geologia.min_diccionario md4 ON md4.id = im.min_4
     LEFT JOIN geologia.min_distribucion dist4 ON dist4.id = im.min_dist_4
  WHERE im.eliminado_en IS NULL
  ORDER BY s.codigo, im.desde;

CREATE OR REPLACE VIEW logueo.vw_mm_estructural AS
SELECT est.id AS estructura_id,
    son.codigo AS sondaje_codigo,
    est.desde,
    est.hasta,
    est.profundidad,
    est.longitud,
    est.alpha_angle,
    est.beta_angle,
    est.dip,
    dic.codigo AS tipo_estructura,
    ven.codigo AS tipo_venilla,
    esp.codigo AS espesor,
    for_est.codigo AS forma,
    rug.codigo AS rugosidad,
    rel1.codigo AS relleno_1,
    rel2.codigo AS relleno_2,
    lit.codigo AS litologia,
    est.orientacion,
    est.comentarios,
    est.fecha,
    est.geologo
   FROM logueo.iestructural est
     LEFT JOIN operacional.prf_sondaje son ON est.sondaje_id = son.id
     LEFT JOIN geologia.est_diccionario dic ON est.tipo_estructura_id = dic.id
     LEFT JOIN geologia.lit_diccionario lit ON est.litologia_id = lit.id
     LEFT JOIN geologia.est_tipo_venilla ven ON est.tipo_venilla_id = ven.id
     LEFT JOIN geologia.est_espesor esp ON est.espesor_id = esp.id
     LEFT JOIN geologia.est_forma for_est ON est.forma_id = for_est.id
     LEFT JOIN geologia.est_rugosidad rug ON est.rugosidad_id = rug.id
     LEFT JOIN geologia.est_relleno rel1 ON est.relleno_1_id = rel1.id
     LEFT JOIN geologia.est_relleno rel2 ON est.relleno_2_id = rel2.id
  WHERE est.eliminado_en IS NULL
  ORDER BY son.codigo, est.desde;

CREATE OR REPLACE VIEW logueo.vw_mm_quicklog AS
SELECT q.id AS quicklog_id,
    son.codigo AS sondaje_codigo,
    q.desde,
    q.hasta,
    q.longitud,
    lit.codigo AS litologia,
    alt1.nombre AS alter_tipo_1,
    alt2.nombre AS alter_tipo_2,
    int1.nombre AS alter_intensidad_1,
    int2.nombre AS alter_intensidad_2,
    aso1.nombre AS min_asociado_1,
    aso2.nombre AS min_asociado_2,
    aso3.nombre AS min_asociado_3,
    min1.nombre AS min_tipo_1,
    est1.nombre AS min_estilo_1,
    min2.nombre AS min_tipo_2,
    est2.nombre AS min_estilo_2,
    min3.nombre AS min_tipo_3,
    est3.nombre AS min_estilo_3,
    q.pc_py,
    q.pc_cu,
    q.pc_mo,
    zona.nombre AS zona_mineral,
    q.comentarios,
    q.fecha,
    q.geologo
   FROM logueo.iquicklog q
     LEFT JOIN operacional.prf_sondaje son ON q.sondaje_id = son.id
     LEFT JOIN geologia.lit_diccionario lit ON q.lito_id = lit.id
     LEFT JOIN geologia.alt_diccionario alt1 ON q.alt_tipo_1_id = alt1.id
     LEFT JOIN geologia.alt_diccionario alt2 ON q.alt_tipo_2_id = alt2.id
     LEFT JOIN geologia.alt_intensidad int1 ON q.alt_intensidad_1_id = int1.id
     LEFT JOIN geologia.alt_intensidad int2 ON q.alt_intensidad_2_id = int2.id
     LEFT JOIN geologia.alt_mineral aso1 ON q.alt_aso_1_id = aso1.id
     LEFT JOIN geologia.alt_mineral aso2 ON q.alt_aso_2_id = aso2.id
     LEFT JOIN geologia.alt_mineral aso3 ON q.alt_aso_3_id = aso3.id
     LEFT JOIN geologia.min_diccionario min1 ON q.min_tipo_1_id = min1.id
     LEFT JOIN geologia.min_diccionario min2 ON q.min_tipo_2_id = min2.id
     LEFT JOIN geologia.min_diccionario min3 ON q.min_tipo_3_id = min3.id
     LEFT JOIN geologia.min_distribucion est1 ON q.min_estilo_1_id = est1.id
     LEFT JOIN geologia.min_distribucion est2 ON q.min_estilo_2_id = est2.id
     LEFT JOIN geologia.min_distribucion est3 ON q.min_estilo_3_id = est3.id
     LEFT JOIN geologia.min_zona zona ON q.min_zona_id = zona.id
  WHERE q.eliminado_en IS NULL
  ORDER BY son.codigo, q.desde;

-- Las restricciones de no solapamiento ignoran lo borrado. Si no, un tramo
-- eliminado seguiría ocupando su intervalo y el geólogo no podría volver a
-- loguear ese metro de testigo.
ALTER TABLE logueo.ialteracion DROP CONSTRAINT IF EXISTS ialt_no_overlap;
ALTER TABLE logueo.ialteracion ADD CONSTRAINT ialt_no_overlap
    EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)') WITH &&)
    WHERE (eliminado_en IS NULL);
ALTER TABLE logueo.ilitologia DROP CONSTRAINT IF EXISTS ilito_no_overlap;
ALTER TABLE logueo.ilitologia ADD CONSTRAINT ilito_no_overlap
    EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)') WITH &&)
    WHERE (eliminado_en IS NULL);
ALTER TABLE logueo.imineralizacion DROP CONSTRAINT IF EXISTS imin_no_overlap;
ALTER TABLE logueo.imineralizacion ADD CONSTRAINT imin_no_overlap
    EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)') WITH &&)
    WHERE (eliminado_en IS NULL);
ALTER TABLE logueo.iquicklog DROP CONSTRAINT IF EXISTS iquick_no_overlap;
ALTER TABLE logueo.iquicklog ADD CONSTRAINT iquick_no_overlap
    EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)') WITH &&)
    WHERE (eliminado_en IS NULL);
ALTER TABLE logueo.iestructural DROP CONSTRAINT IF EXISTS iestruc_no_overlap;
ALTER TABLE logueo.iestructural ADD CONSTRAINT iestruc_no_overlap
    EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)') WITH &&)
    WHERE (eliminado_en IS NULL AND desde IS NOT NULL AND hasta IS NOT NULL);

-- ===== 2. Columnas nuevas en tablas existentes =====
ALTER TABLE operacional.prf_survey ADD COLUMN IF NOT EXISTS medicion_id uuid;
ALTER TABLE base.empresa ADD COLUMN IF NOT EXISTS correo   character varying(100);
ALTER TABLE base.empresa ADD COLUMN IF NOT EXISTS telefono character varying(50);

-- Único cambio sobre columnas existentes: de ancho fijo a variable. El valor es
-- el mismo sin los espacios de relleno a la derecha.
ALTER TABLE base.empresa ALTER COLUMN codigo TYPE character varying(50) USING rtrim(codigo);
ALTER TABLE base.empresa ALTER COLUMN ruc    TYPE character varying(20) USING rtrim(ruc);

-- ===== 3. Tablas, secuencias, índices, disparadores y vistas que faltan =====
--
-- PostgreSQL database dump
--

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

--
-- Name: fot_foto_tocar(); Type: FUNCTION; Schema: operacional; Owner: -
--

CREATE FUNCTION operacional.fot_foto_tocar() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN NEW.actualizado_en := now(); RETURN NEW; END; $$;

--
-- Name: cargo; Type: TABLE; Schema: base; Owner: -
--

CREATE TABLE base.cargo (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    codigo character varying(20) NOT NULL,
    nombre character varying(60) NOT NULL,
    area character varying(20) NOT NULL,
    estado boolean DEFAULT true NOT NULL,
    CONSTRAINT cargo_area_chk CHECK (((area)::text = ANY (ARRAY[('perforacion'::character varying)::text, ('geologia'::character varying)::text, ('laboratorio'::character varying)::text, ('administracion'::character varying)::text])))
);

--
-- Name: cargo_numero_interno_seq; Type: SEQUENCE; Schema: base; Owner: -
--

CREATE SEQUENCE base.cargo_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: cargo_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: base; Owner: -
--

ALTER SEQUENCE base.cargo_numero_interno_seq OWNED BY base.cargo.numero_interno;

--
-- Name: empleado; Type: TABLE; Schema: base; Owner: -
--

CREATE TABLE base.empleado (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    codigo character varying(15) NOT NULL,
    nombres character varying(80) NOT NULL,
    apellidos character varying(80),
    documento character varying(20),
    empresa_id uuid,
    cargo_id uuid,
    email character varying(120),
    telefono character varying(30),
    fecha_ingreso date,
    fecha_salida date,
    estado boolean DEFAULT true NOT NULL,
    notas text,
    CONSTRAINT empleado_fechas_chk CHECK (((fecha_salida IS NULL) OR (fecha_ingreso IS NULL) OR (fecha_salida >= fecha_ingreso)))
);

--
-- Name: empleado_numero_interno_seq; Type: SEQUENCE; Schema: base; Owner: -
--

CREATE SEQUENCE base.empleado_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: empleado_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: base; Owner: -
--

ALTER SEQUENCE base.empleado_numero_interno_seq OWNED BY base.empleado.numero_interno;

--
-- Name: rol; Type: TABLE; Schema: base; Owner: -
--

CREATE TABLE base.rol (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(100) NOT NULL
);

--
-- Name: rol_numero_interno_seq; Type: SEQUENCE; Schema: base; Owner: -
--

CREATE SEQUENCE base.rol_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: rol_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: base; Owner: -
--

ALTER SEQUENCE base.rol_numero_interno_seq OWNED BY base.rol.numero_interno;

--
-- Name: usuario; Type: TABLE; Schema: base; Owner: -
--

CREATE TABLE base.usuario (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    nombres character varying(100) NOT NULL,
    apellidos character varying(100) NOT NULL,
    correo character varying(100) NOT NULL,
    usuario character varying(50) NOT NULL,
    contrasenia character varying(255) NOT NULL,
    estado boolean DEFAULT true NOT NULL,
    empleado_id uuid
);

--
-- Name: usuario_numero_interno_seq; Type: SEQUENCE; Schema: base; Owner: -
--

CREATE SEQUENCE base.usuario_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: usuario_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: base; Owner: -
--

ALTER SEQUENCE base.usuario_numero_interno_seq OWNED BY base.usuario.numero_interno;

--
-- Name: usuario_rol; Type: TABLE; Schema: base; Owner: -
--

CREATE TABLE base.usuario_rol (
    id_usuario uuid NOT NULL,
    id_rol uuid NOT NULL,
    asignado_el timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    id uuid DEFAULT gen_random_uuid() NOT NULL
);

--
-- Name: dispositivo_sync; Type: TABLE; Schema: logueo; Owner: -
--

CREATE TABLE logueo.dispositivo_sync (
    dispositivo character varying(64) NOT NULL,
    usuario character varying(80),
    version_app character varying(20),
    ultimo_contacto timestamp with time zone DEFAULT now() NOT NULL,
    ultima_subida timestamp with time zone,
    filas_subidas integer DEFAULT 0 NOT NULL
);

--
-- Name: TABLE dispositivo_sync; Type: COMMENT; Schema: logueo; Owner: -
--

COMMENT ON TABLE logueo.dispositivo_sync IS 'Última sincronización de cada tablet de GeoLog; la actualiza el backend en cada subida.';

--
-- Name: crt_densidad; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.crt_densidad (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid,
    sondaje_codigo character varying(30),
    fecha date NOT NULL,
    turno character varying(10),
    responsable character varying(80),
    desde numeric(9,2) NOT NULL,
    hasta numeric(9,2) NOT NULL,
    total_muestras integer NOT NULL,
    total_metraje numeric(9,2),
    tipo_linea character varying(10),
    estado character varying(20) DEFAULT 'registrado'::character varying NOT NULL,
    observaciones text,
    CONSTRAINT crt_densidad_ident_chk CHECK (((sondaje_id IS NOT NULL) OR (sondaje_codigo IS NOT NULL))),
    CONSTRAINT crt_densidad_musetras_chk CHECK ((total_muestras >= 0)),
    CONSTRAINT crt_densidad_rango_chk CHECK ((hasta >= desde))
);

--
-- Name: crt_densidad_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.crt_densidad_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: crt_densidad_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.crt_densidad_numero_interno_seq OWNED BY operacional.crt_densidad.numero_interno;

--
-- Name: crt_muestra; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.crt_muestra (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid,
    sondaje_codigo character varying(30),
    fecha date NOT NULL,
    turno character varying(10),
    responsable character varying(80),
    desde numeric(9,2) NOT NULL,
    hasta numeric(9,2) NOT NULL,
    tipo_linea character varying(10),
    metraje_estimado numeric(9,2),
    nro_muestras integer,
    estado character varying(20) DEFAULT 'registrado'::character varying NOT NULL,
    observaciones text,
    CONSTRAINT crt_muestra_ident_chk CHECK (((sondaje_id IS NOT NULL) OR (sondaje_codigo IS NOT NULL))),
    CONSTRAINT crt_muestra_rango_chk CHECK ((hasta >= desde))
);

--
-- Name: crt_muestra_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.crt_muestra_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: crt_muestra_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.crt_muestra_numero_interno_seq OWNED BY operacional.crt_muestra.numero_interno;

--
-- Name: dns_avance; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.dns_avance (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid,
    sondaje_codigo character varying(30),
    fecha date NOT NULL,
    turno character varying(10),
    responsable character varying(80),
    etapa character varying(20) NOT NULL,
    nro_muestras integer NOT NULL,
    temperatura_c numeric(5,1),
    tipo_linea character varying(10),
    desde numeric(9,2),
    hasta numeric(9,2),
    estado character varying(20) DEFAULT 'registrado'::character varying NOT NULL,
    observaciones text,
    CONSTRAINT dns_avance_etapa_chk CHECK (((etapa)::text = ANY (ARRAY[('corte'::character varying)::text, ('secado_1'::character varying)::text, ('secado_2'::character varying)::text, ('pesado'::character varying)::text, ('calculo'::character varying)::text]))),
    CONSTRAINT dns_avance_ident_chk CHECK (((sondaje_id IS NOT NULL) OR (sondaje_codigo IS NOT NULL))),
    CONSTRAINT dns_avance_nro_chk CHECK ((nro_muestras >= 0)),
    CONSTRAINT dns_avance_rango_chk CHECK (((hasta IS NULL) OR (desde IS NULL) OR (hasta >= desde)))
);

--
-- Name: dns_avance_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.dns_avance_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: dns_avance_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.dns_avance_numero_interno_seq OWNED BY operacional.dns_avance.numero_interno;

--
-- Name: fot_avance; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.fot_avance (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid,
    sondaje_codigo character varying(30),
    fecha date NOT NULL,
    turno character varying(10),
    responsable character varying(80),
    box_desde integer,
    box_hasta integer,
    total_cajas integer,
    marcado_desde numeric(9,2),
    marcado_hasta numeric(9,2),
    total_metraje numeric(9,2),
    seco_humedo character varying(10),
    nro_fotos integer,
    ruta_archivos text,
    estado character varying(20) DEFAULT 'registrado'::character varying NOT NULL,
    observaciones text,
    CONSTRAINT fot_avance_box_chk CHECK (((box_hasta IS NULL) OR (box_desde IS NULL) OR (box_hasta >= box_desde))),
    CONSTRAINT fot_avance_ident_chk CHECK (((sondaje_id IS NOT NULL) OR (sondaje_codigo IS NOT NULL))),
    CONSTRAINT fot_avance_seco_chk CHECK (((seco_humedo IS NULL) OR ((seco_humedo)::text = ANY (ARRAY[('seco'::character varying)::text, ('humedo'::character varying)::text]))))
);

--
-- Name: fot_avance_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.fot_avance_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: fot_avance_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.fot_avance_numero_interno_seq OWNED BY operacional.fot_avance.numero_interno;

--
-- Name: fot_foto; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.fot_foto (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid NOT NULL,
    caja_desde integer,
    caja_hasta integer,
    desde numeric(9,2),
    hasta numeric(9,2),
    condicion character varying(10),
    tomada_en timestamp with time zone,
    autor character varying(80),
    dispositivo character varying(64),
    archivo_original character varying(255),
    tipo_mime character varying(40) NOT NULL,
    bytes bigint NOT NULL,
    ancho integer,
    alto integer,
    sha256 character varying(64) NOT NULL,
    clave_almacen character varying(255) NOT NULL,
    observaciones text,
    eliminado_en timestamp with time zone,
    creado_en timestamp with time zone DEFAULT now() NOT NULL,
    actualizado_en timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT fot_foto_bytes_chk CHECK ((bytes > 0)),
    CONSTRAINT fot_foto_cajas_chk CHECK (((caja_hasta IS NULL) OR (caja_desde IS NULL) OR (caja_hasta >= caja_desde))),
    CONSTRAINT fot_foto_condicion_chk CHECK (((condicion IS NULL) OR ((condicion)::text = ANY ((ARRAY['seco'::character varying, 'humedo'::character varying])::text[])))),
    CONSTRAINT fot_foto_tramo_chk CHECK (((hasta IS NULL) OR (desde IS NULL) OR (hasta >= desde)))
);

--
-- Name: TABLE fot_foto; Type: COMMENT; Schema: operacional; Owner: -
--

COMMENT ON TABLE operacional.fot_foto IS 'Fotos de cajas de testigo. El binario vive en el almacén de archivos; aquí, la clave y los metadatos.';

--
-- Name: COLUMN fot_foto.sha256; Type: COMMENT; Schema: operacional; Owner: -
--

COMMENT ON COLUMN operacional.fot_foto.sha256 IS 'Hash del archivo original: verifica que la subida llegó entera y detecta la misma foto subida dos veces.';

--
-- Name: COLUMN fot_foto.clave_almacen; Type: COMMENT; Schema: operacional; Owner: -
--

COMMENT ON COLUMN operacional.fot_foto.clave_almacen IS 'Ruta relativa dentro del almacén de fotos (p. ej. DDH-VERO-S-040-26/<id>.jpg).';

--
-- Name: fot_foto_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.fot_foto_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: fot_foto_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.fot_foto_numero_interno_seq OWNED BY operacional.fot_foto.numero_interno;

--
-- Name: log_avance; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.log_avance (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid,
    sondaje_codigo character varying(30),
    fecha date NOT NULL,
    turno character varying(10),
    tipo_logueo character varying(20) NOT NULL,
    desde numeric(9,2) NOT NULL,
    hasta numeric(9,2) NOT NULL,
    avance numeric(9,2) NOT NULL,
    geologo character varying(80),
    pct_completado numeric(5,2),
    estado character varying(20) DEFAULT 'registrado'::character varying NOT NULL,
    observaciones text,
    CONSTRAINT log_avance_avance_chk CHECK ((avance >= (0)::numeric)),
    CONSTRAINT log_avance_ident_chk CHECK (((sondaje_id IS NOT NULL) OR (sondaje_codigo IS NOT NULL))),
    CONSTRAINT log_avance_pct_chk CHECK (((pct_completado IS NULL) OR ((pct_completado >= (0)::numeric) AND (pct_completado <= (100)::numeric)))),
    CONSTRAINT log_avance_rango_chk CHECK ((hasta >= desde)),
    CONSTRAINT log_avance_tipo_chk CHECK (((tipo_logueo)::text = ANY (ARRAY[('geologico'::character varying)::text, ('geotecnico'::character varying)::text, ('estructural'::character varying)::text, ('rapido'::character varying)::text]))),
    CONSTRAINT log_avance_turno_chk CHECK (((turno IS NULL) OR ((turno)::text = ANY (ARRAY[('dia'::character varying)::text, ('noche'::character varying)::text]))))
);

--
-- Name: log_avance_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.log_avance_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: log_avance_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.log_avance_numero_interno_seq OWNED BY operacional.log_avance.numero_interno;

--
-- Name: log_recepcion_caja; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.log_recepcion_caja (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid,
    sondaje_codigo character varying(30),
    fecha date NOT NULL,
    turno character varying(10),
    responsable character varying(80),
    box_desde integer,
    box_hasta integer,
    total_cajas integer,
    marcado_desde numeric(9,2),
    marcado_hasta numeric(9,2),
    total_metraje numeric(9,2),
    estado character varying(20) DEFAULT 'registrado'::character varying NOT NULL,
    observaciones text,
    CONSTRAINT log_recepcion_caja_box_chk CHECK (((box_hasta IS NULL) OR (box_desde IS NULL) OR (box_hasta >= box_desde))),
    CONSTRAINT log_recepcion_caja_cajas_chk CHECK (((total_cajas IS NULL) OR (total_cajas >= 0))),
    CONSTRAINT log_recepcion_caja_ident_chk CHECK (((sondaje_id IS NOT NULL) OR (sondaje_codigo IS NOT NULL)))
);

--
-- Name: log_recepcion_caja_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.log_recepcion_caja_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: log_recepcion_caja_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.log_recepcion_caja_numero_interno_seq OWNED BY operacional.log_recepcion_caja.numero_interno;

--
-- Name: mst_avance; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.mst_avance (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid,
    sondaje_codigo character varying(30),
    fecha date NOT NULL,
    turno character varying(10),
    responsable character varying(80),
    estado_muestreo character varying(15) NOT NULL,
    desde numeric(9,2),
    hasta numeric(9,2),
    avance numeric(9,2),
    nro_muestras integer,
    nro_muestras_embolsadas integer,
    nro_costales integer,
    nro_lotes integer,
    estado character varying(20) DEFAULT 'registrado'::character varying NOT NULL,
    observaciones text,
    CONSTRAINT mst_avance_estado_chk CHECK (((estado_muestreo)::text = ANY (ARRAY[('en_proceso'::character varying)::text, ('finalizado'::character varying)::text]))),
    CONSTRAINT mst_avance_ident_chk CHECK (((sondaje_id IS NOT NULL) OR (sondaje_codigo IS NOT NULL))),
    CONSTRAINT mst_avance_rango_chk CHECK (((hasta IS NULL) OR (desde IS NULL) OR (hasta >= desde)))
);

--
-- Name: mst_avance_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.mst_avance_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: mst_avance_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.mst_avance_numero_interno_seq OWNED BY operacional.mst_avance.numero_interno;

--
-- Name: prf_avance; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_avance (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid,
    sondaje_codigo character varying(30),
    fecha date NOT NULL,
    turno character varying(10),
    responsable character varying(80),
    maquina_id uuid,
    maquina_codigo character varying(20),
    plataforma_id uuid,
    plataforma_codigo character varying(20),
    desde numeric(9,2) NOT NULL,
    hasta numeric(9,2) NOT NULL,
    avance numeric(9,2) NOT NULL,
    casing_tipo character varying(15),
    casing_desde numeric(9,2),
    casing_hasta numeric(9,2),
    retorno_pct numeric(5,2),
    linea character varying(10),
    comentarios text,
    estado character varying(20) DEFAULT 'registrado'::character varying NOT NULL,
    observaciones text,
    CONSTRAINT prf_avance_avance_chk CHECK ((avance >= (0)::numeric)),
    CONSTRAINT prf_avance_ident_chk CHECK (((sondaje_id IS NOT NULL) OR (sondaje_codigo IS NOT NULL))),
    CONSTRAINT prf_avance_rango_chk CHECK ((hasta >= desde)),
    CONSTRAINT prf_avance_retorno_chk CHECK (((retorno_pct IS NULL) OR ((retorno_pct >= (0)::numeric) AND (retorno_pct <= (100)::numeric)))),
    CONSTRAINT prf_avance_turno_chk CHECK (((turno IS NULL) OR ((turno)::text = ANY (ARRAY[('dia'::character varying)::text, ('noche'::character varying)::text]))))
);

--
-- Name: prf_avance_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_avance_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: prf_avance_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_avance_numero_interno_seq OWNED BY operacional.prf_avance.numero_interno;

--
-- Name: prf_avance_personal; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_avance_personal (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    avance_id uuid NOT NULL,
    perforista_id uuid,
    nombre character varying(80) NOT NULL,
    cargo character varying(40),
    codigo_maquina character varying(20)
);

--
-- Name: prf_proyecto_maquina; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_proyecto_maquina (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    proyecto_id uuid NOT NULL,
    maquina_id uuid NOT NULL,
    fecha_ingreso date NOT NULL,
    fecha_salida date,
    estado character varying(30),
    CONSTRAINT prf_proymaq_fechas_chk CHECK (((fecha_salida IS NULL) OR (fecha_salida >= fecha_ingreso)))
);

--
-- Name: prf_proyecto_maquina_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_proyecto_maquina_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: prf_proyecto_maquina_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_proyecto_maquina_numero_interno_seq OWNED BY operacional.prf_proyecto_maquina.numero_interno;

--
-- Name: prf_survey_medicion; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_survey_medicion (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid NOT NULL,
    maquina_id uuid,
    perforista_id uuid,
    profundidad numeric(9,2) NOT NULL,
    nro_medicion smallint,
    fecha_medicion date NOT NULL,
    fecha_hora timestamp with time zone,
    responsable character varying(80),
    metodo character varying(50),
    instrumento character varying(50),
    azimut_medido double precision,
    inclinacion_medida double precision,
    azimut_plan double precision,
    inclinacion_plan double precision,
    desv_azimut double precision,
    desv_azimut_sentido character varying(10),
    desv_inclinacion double precision,
    desv_inclinacion_sentido character varying(10),
    desv_metros double precision,
    desv_total double precision,
    es_planificada boolean DEFAULT false NOT NULL,
    decision character varying(20),
    observaciones text,
    interpretacion text,
    instruccion text,
    responsable_seguimiento character varying(80),
    estado_seguimiento character varying(20) DEFAULT 'sin_seguimiento'::character varying NOT NULL,
    CONSTRAINT prf_survey_medicion_azs_chk CHECK (((desv_azimut_sentido IS NULL) OR ((desv_azimut_sentido)::text = ANY (ARRAY[('izquierda'::character varying)::text, ('derecha'::character varying)::text])))),
    CONSTRAINT prf_survey_medicion_decision_chk CHECK (((decision IS NULL) OR ((decision)::text = ANY (ARRAY[('continuar'::character varying)::text, ('corregir'::character varying)::text, ('evaluar'::character varying)::text])))),
    CONSTRAINT prf_survey_medicion_dis_chk CHECK (((desv_inclinacion_sentido IS NULL) OR ((desv_inclinacion_sentido)::text = ANY (ARRAY[('arriba'::character varying)::text, ('abajo'::character varying)::text])))),
    CONSTRAINT prf_survey_medicion_prof_chk CHECK ((profundidad >= (0)::numeric)),
    CONSTRAINT prf_survey_medicion_seguimiento_chk CHECK (((estado_seguimiento)::text = ANY ((ARRAY['sin_seguimiento'::character varying, 'pendiente'::character varying, 'en_curso'::character varying, 'cerrado'::character varying])::text[])))
);

--
-- Name: COLUMN prf_survey_medicion.interpretacion; Type: COMMENT; Schema: operacional; Owner: -
--

COMMENT ON COLUMN operacional.prf_survey_medicion.interpretacion IS 'Por qué se desvió: la lectura geológica del desvío, no el número.';

--
-- Name: COLUMN prf_survey_medicion.instruccion; Type: COMMENT; Schema: operacional; Owner: -
--

COMMENT ON COLUMN operacional.prf_survey_medicion.instruccion IS 'Qué hacer: la indicación operativa que se le da a la máquina.';

--
-- Name: COLUMN prf_survey_medicion.responsable_seguimiento; Type: COMMENT; Schema: operacional; Owner: -
--

COMMENT ON COLUMN operacional.prf_survey_medicion.responsable_seguimiento IS 'A quién se le encarga el seguimiento de la instrucción.';

--
-- Name: COLUMN prf_survey_medicion.estado_seguimiento; Type: COMMENT; Schema: operacional; Owner: -
--

COMMENT ON COLUMN operacional.prf_survey_medicion.estado_seguimiento IS 'sin_seguimiento | pendiente | en_curso | cerrado';

--
-- Name: prf_survey_medicion_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_survey_medicion_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: prf_survey_medicion_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_survey_medicion_numero_interno_seq OWNED BY operacional.prf_survey_medicion.numero_interno;

--
-- Name: vw_avance_perforacion_diario; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_avance_perforacion_diario AS
 SELECT COALESCE(s.codigo, a.sondaje_codigo) AS sondaje,
    a.fecha,
    a.turno,
    a.maquina_codigo,
    a.linea,
    a.desde,
    a.hasta,
    a.avance,
    a.retorno_pct,
    sum(a.avance) OVER (PARTITION BY COALESCE(s.codigo, a.sondaje_codigo) ORDER BY a.fecha, a.turno ROWS UNBOUNDED PRECEDING) AS avance_acumulado
   FROM (operacional.prf_avance a
     LEFT JOIN operacional.prf_sondaje s ON ((s.id = a.sondaje_id)))
  ORDER BY COALESCE(s.codigo, a.sondaje_codigo), a.fecha, a.turno;

--
-- Name: vw_desviacion_por_maquina; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_desviacion_por_maquina AS
 SELECT mq.id AS maquina_id,
    mq.codigo AS maquina_codigo,
    mq.nombre AS maquina_nombre,
    count(m.id) AS n_mediciones,
    round((avg(abs(m.desv_azimut)))::numeric, 4) AS desv_az_prom_abs,
    round((max(abs(m.desv_azimut)))::numeric, 4) AS desv_az_max_abs,
    round((avg(abs(m.desv_inclinacion)))::numeric, 4) AS desv_dip_prom_abs,
    round((max(abs(m.desv_inclinacion)))::numeric, 4) AS desv_dip_max_abs,
    round((avg(abs(m.desv_metros)))::numeric, 4) AS desv_metros_prom,
    round((max(abs(m.desv_metros)))::numeric, 4) AS desv_metros_max,
    count(*) FILTER (WHERE ((m.decision)::text = 'corregir'::text)) AS n_correcciones
   FROM (operacional.prf_survey_medicion m
     JOIN operacional.prf_maquina mq ON ((mq.id = m.maquina_id)))
  GROUP BY mq.id, mq.codigo, mq.nombre;

--
-- Name: vw_desviacion_por_perforista; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_desviacion_por_perforista AS
 SELECT p.id AS perforista_id,
    p.codigo AS perforista_codigo,
    (((p.nombres)::text || ' '::text) || (COALESCE(p.apellidos, ''::character varying))::text) AS perforista,
    count(m.id) AS n_mediciones,
    round((avg(abs(m.desv_azimut)))::numeric, 4) AS desv_az_prom_abs,
    round((max(abs(m.desv_azimut)))::numeric, 4) AS desv_az_max_abs,
    round((avg(abs(m.desv_inclinacion)))::numeric, 4) AS desv_dip_prom_abs,
    round((max(abs(m.desv_inclinacion)))::numeric, 4) AS desv_dip_max_abs,
    round((avg(abs(m.desv_metros)))::numeric, 4) AS desv_metros_prom,
    round((max(abs(m.desv_metros)))::numeric, 4) AS desv_metros_max,
    count(*) FILTER (WHERE ((m.decision)::text = 'corregir'::text)) AS n_correcciones
   FROM (operacional.prf_survey_medicion m
     JOIN base.empleado p ON ((p.id = m.perforista_id)))
  GROUP BY p.id, p.codigo, p.nombres, p.apellidos;

--
-- Name: vw_desviacion_por_sondaje; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_desviacion_por_sondaje AS
 SELECT s.codigo AS sondaje_codigo,
    s.nombre AS sondaje_nombre,
    m.profundidad,
    m.nro_medicion,
    m.fecha_medicion,
    mq.codigo AS maquina_codigo,
    (((p.nombres)::text || ' '::text) || (COALESCE(p.apellidos, ''::character varying))::text) AS perforista,
    m.desv_azimut,
    m.desv_azimut_sentido,
    m.desv_inclinacion,
    m.desv_inclinacion_sentido,
    m.desv_metros,
    m.desv_total,
    m.decision,
    m.id AS medicion_id,
    s.id AS sondaje_id,
    m.interpretacion,
    m.instruccion,
    m.responsable_seguimiento,
    m.estado_seguimiento
   FROM (((operacional.prf_survey_medicion m
     JOIN operacional.prf_sondaje s ON ((s.id = m.sondaje_id)))
     LEFT JOIN operacional.prf_maquina mq ON ((mq.id = m.maquina_id)))
     LEFT JOIN base.empleado p ON ((p.id = m.perforista_id)))
  ORDER BY s.codigo, m.profundidad, m.nro_medicion;

--
-- Name: vw_mm_litologia; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_mm_litologia AS
 SELECT id,
    sondaje_codigo,
    sondaje_nombre,
    desde,
    hasta,
    longitud,
    tipo_material,
    color,
    intensidad_color,
    grupo_litologico,
    tipo_litologia,
    tipo_subroca,
    descripcion_roca,
    tamanio_grano,
    textura,
    bx_pc_clasto,
    bx_forma,
    bx_pc_matriz,
    bx_tipo_matriz,
    comentarios,
    fecha,
    geologo
   FROM logueo.vw_mm_litologia;

--
-- Name: vw_mm_mineralizacion; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_mm_mineralizacion AS
 SELECT id,
    sondaje_codigo,
    sondaje_nombre,
    desde,
    hasta,
    longitud,
    zona_mineralizacion,
    min_1,
    min_pc_1,
    min_dist_1,
    cu_m_pc_1,
    cu_pc_1,
    mo_m_pc_1,
    mo_pc_1,
    min_2,
    min_pc_2,
    min_dist_2,
    cu_m_pc_2,
    cu_pc_2,
    mo_m_pc_2,
    mo_pc_2,
    min_3,
    min_pc_3,
    min_dist_3,
    cu_m_pc_3,
    cu_pc_3,
    mo_m_pc_3,
    mo_pc_3,
    min_4,
    min_pc_4,
    min_dist_4,
    cu_m_pc_4,
    cu_pc_4,
    mo_m_pc_4,
    mo_pc_4,
    cu_pct,
    mo_pct,
    descripcion,
    fecha,
    geologo
   FROM logueo.vw_mm_mineralizacion;

--
-- Name: vw_sondaje_micromine; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_sondaje_micromine AS
 SELECT s.id,
    s.codigo,
    s.nombre,
    c.este,
    c.norte,
    c.elevacion,
    s.azimut_setup,
    s.inclinacion_setup,
    sv.profundidad,
    sv.azimut,
    sv.inclinacion,
    s.profundidad_final,
    s.fecha_inicio,
    s.fecha_fin
   FROM ((operacional.prf_sondaje s
     JOIN operacional.prf_collar c ON ((c.id = s.collar_id)))
     LEFT JOIN operacional.prf_survey sv ON ((sv.sondaje_id = s.id)));

--
-- Name: cargo numero_interno; Type: DEFAULT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.cargo ALTER COLUMN numero_interno SET DEFAULT nextval('base.cargo_numero_interno_seq'::regclass);

--
-- Name: empleado numero_interno; Type: DEFAULT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.empleado ALTER COLUMN numero_interno SET DEFAULT nextval('base.empleado_numero_interno_seq'::regclass);

--
-- Name: rol numero_interno; Type: DEFAULT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.rol ALTER COLUMN numero_interno SET DEFAULT nextval('base.rol_numero_interno_seq'::regclass);

--
-- Name: usuario numero_interno; Type: DEFAULT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario ALTER COLUMN numero_interno SET DEFAULT nextval('base.usuario_numero_interno_seq'::regclass);

--
-- Name: crt_densidad numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.crt_densidad ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.crt_densidad_numero_interno_seq'::regclass);

--
-- Name: crt_muestra numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.crt_muestra ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.crt_muestra_numero_interno_seq'::regclass);

--
-- Name: dns_avance numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.dns_avance ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.dns_avance_numero_interno_seq'::regclass);

--
-- Name: fot_avance numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.fot_avance ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.fot_avance_numero_interno_seq'::regclass);

--
-- Name: fot_foto numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.fot_foto ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.fot_foto_numero_interno_seq'::regclass);

--
-- Name: log_avance numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.log_avance ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.log_avance_numero_interno_seq'::regclass);

--
-- Name: log_recepcion_caja numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.log_recepcion_caja ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.log_recepcion_caja_numero_interno_seq'::regclass);

--
-- Name: mst_avance numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.mst_avance ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.mst_avance_numero_interno_seq'::regclass);

--
-- Name: prf_avance numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_avance ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_avance_numero_interno_seq'::regclass);

--
-- Name: prf_proyecto_maquina numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_proyecto_maquina ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_proyecto_maquina_numero_interno_seq'::regclass);

--
-- Name: prf_survey_medicion numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey_medicion ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_survey_medicion_numero_interno_seq'::regclass);

--
-- Name: cargo cargo_cod_uq; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.cargo
    ADD CONSTRAINT cargo_cod_uq UNIQUE (codigo);

--
-- Name: cargo cargo_pk; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.cargo
    ADD CONSTRAINT cargo_pk PRIMARY KEY (id);

--
-- Name: empleado empleado_cod_uq; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.empleado
    ADD CONSTRAINT empleado_cod_uq UNIQUE (codigo);

--
-- Name: empleado empleado_pk; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.empleado
    ADD CONSTRAINT empleado_pk PRIMARY KEY (id);

--
-- Name: rol rol_codigo_uq; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.rol
    ADD CONSTRAINT rol_codigo_uq UNIQUE (codigo);

--
-- Name: rol role_pk; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.rol
    ADD CONSTRAINT role_pk PRIMARY KEY (id);

--
-- Name: usuario usuario_correo_uq; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario
    ADD CONSTRAINT usuario_correo_uq UNIQUE (correo);

--
-- Name: usuario usuario_empleado_uq; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario
    ADD CONSTRAINT usuario_empleado_uq UNIQUE (empleado_id);

--
-- Name: usuario usuario_pk; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario
    ADD CONSTRAINT usuario_pk PRIMARY KEY (id);

--
-- Name: usuario_rol usuario_rol_pk; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario_rol
    ADD CONSTRAINT usuario_rol_pk PRIMARY KEY (id);

--
-- Name: usuario_rol usuario_rol_uq; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario_rol
    ADD CONSTRAINT usuario_rol_uq UNIQUE (id_usuario, id_rol);

--
-- Name: usuario usuario_usuario_uq; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario
    ADD CONSTRAINT usuario_usuario_uq UNIQUE (usuario);

--
-- Name: dispositivo_sync dispositivo_sync_pkey; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.dispositivo_sync
    ADD CONSTRAINT dispositivo_sync_pkey PRIMARY KEY (dispositivo);

--
-- Name: crt_densidad crt_densidad_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.crt_densidad
    ADD CONSTRAINT crt_densidad_pk PRIMARY KEY (id);

--
-- Name: crt_muestra crt_muestra_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.crt_muestra
    ADD CONSTRAINT crt_muestra_pk PRIMARY KEY (id);

--
-- Name: dns_avance dns_avance_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.dns_avance
    ADD CONSTRAINT dns_avance_pk PRIMARY KEY (id);

--
-- Name: fot_avance fot_avance_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.fot_avance
    ADD CONSTRAINT fot_avance_pk PRIMARY KEY (id);

--
-- Name: fot_foto fot_foto_pkey; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.fot_foto
    ADD CONSTRAINT fot_foto_pkey PRIMARY KEY (id);

--
-- Name: log_avance log_avance_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.log_avance
    ADD CONSTRAINT log_avance_pk PRIMARY KEY (id);

--
-- Name: log_recepcion_caja log_recepcion_caja_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.log_recepcion_caja
    ADD CONSTRAINT log_recepcion_caja_pk PRIMARY KEY (id);

--
-- Name: mst_avance mst_avance_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.mst_avance
    ADD CONSTRAINT mst_avance_pk PRIMARY KEY (id);

--
-- Name: prf_avance_personal prf_avance_personal_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_avance_personal
    ADD CONSTRAINT prf_avance_personal_pk PRIMARY KEY (id);

--
-- Name: prf_avance prf_avance_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_avance
    ADD CONSTRAINT prf_avance_pk PRIMARY KEY (id);

--
-- Name: prf_proyecto_maquina prf_proymaq_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_proyecto_maquina
    ADD CONSTRAINT prf_proymaq_pk PRIMARY KEY (id);

--
-- Name: prf_proyecto_maquina prf_proymaq_uq; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_proyecto_maquina
    ADD CONSTRAINT prf_proymaq_uq UNIQUE (proyecto_id, maquina_id, fecha_ingreso);

--
-- Name: prf_survey_medicion prf_survey_medicion_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey_medicion
    ADD CONSTRAINT prf_survey_medicion_pk PRIMARY KEY (id);

--
-- Name: prf_survey_medicion prf_survey_medicion_uq; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey_medicion
    ADD CONSTRAINT prf_survey_medicion_uq UNIQUE (sondaje_id, profundidad, nro_medicion);

--
-- Name: empleado_cargo_idx; Type: INDEX; Schema: base; Owner: -
--

CREATE INDEX empleado_cargo_idx ON base.empleado USING btree (cargo_id);

--
-- Name: empleado_empresa_idx; Type: INDEX; Schema: base; Owner: -
--

CREATE INDEX empleado_empresa_idx ON base.empleado USING btree (empresa_id);

--
-- Name: crt_densidad_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX crt_densidad_fecha_ix ON operacional.crt_densidad USING btree (fecha);

--
-- Name: crt_densidad_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX crt_densidad_sondaje_ix ON operacional.crt_densidad USING btree (sondaje_id);

--
-- Name: crt_muestra_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX crt_muestra_fecha_ix ON operacional.crt_muestra USING btree (fecha);

--
-- Name: crt_muestra_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX crt_muestra_sondaje_ix ON operacional.crt_muestra USING btree (sondaje_id);

--
-- Name: dns_avance_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX dns_avance_fecha_ix ON operacional.dns_avance USING btree (fecha);

--
-- Name: dns_avance_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX dns_avance_sondaje_ix ON operacional.dns_avance USING btree (sondaje_id);

--
-- Name: fot_avance_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX fot_avance_fecha_ix ON operacional.fot_avance USING btree (fecha);

--
-- Name: fot_avance_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX fot_avance_sondaje_ix ON operacional.fot_avance USING btree (sondaje_id);

--
-- Name: fot_foto_sondaje_idx; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX fot_foto_sondaje_idx ON operacional.fot_foto USING btree (sondaje_id, desde) WHERE (eliminado_en IS NULL);

--
-- Name: log_avance_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX log_avance_fecha_ix ON operacional.log_avance USING btree (fecha);

--
-- Name: log_avance_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX log_avance_sondaje_ix ON operacional.log_avance USING btree (sondaje_id);

--
-- Name: log_recepcion_caja_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX log_recepcion_caja_fecha_ix ON operacional.log_recepcion_caja USING btree (fecha);

--
-- Name: log_recepcion_caja_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX log_recepcion_caja_sondaje_ix ON operacional.log_recepcion_caja USING btree (sondaje_id);

--
-- Name: mst_avance_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX mst_avance_fecha_ix ON operacional.mst_avance USING btree (fecha);

--
-- Name: mst_avance_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX mst_avance_sondaje_ix ON operacional.mst_avance USING btree (sondaje_id);

--
-- Name: prf_avance_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_avance_fecha_ix ON operacional.prf_avance USING btree (fecha);

--
-- Name: prf_avance_personal_avance_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_avance_personal_avance_ix ON operacional.prf_avance_personal USING btree (avance_id);

--
-- Name: prf_avance_personal_perforista_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_avance_personal_perforista_ix ON operacional.prf_avance_personal USING btree (perforista_id);

--
-- Name: prf_avance_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_avance_sondaje_ix ON operacional.prf_avance USING btree (sondaje_id);

--
-- Name: prf_proymaq_maquina_idx; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_proymaq_maquina_idx ON operacional.prf_proyecto_maquina USING btree (maquina_id);

--
-- Name: prf_proymaq_proyecto_idx; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_proymaq_proyecto_idx ON operacional.prf_proyecto_maquina USING btree (proyecto_id);

--
-- Name: prf_survey_medicion_fecha_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_survey_medicion_fecha_ix ON operacional.prf_survey_medicion USING btree (fecha_medicion);

--
-- Name: prf_survey_medicion_maquina_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_survey_medicion_maquina_ix ON operacional.prf_survey_medicion USING btree (maquina_id);

--
-- Name: prf_survey_medicion_perforista_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_survey_medicion_perforista_ix ON operacional.prf_survey_medicion USING btree (perforista_id);

--
-- Name: prf_survey_medicion_sondaje_ix; Type: INDEX; Schema: operacional; Owner: -
--

CREATE INDEX prf_survey_medicion_sondaje_ix ON operacional.prf_survey_medicion USING btree (sondaje_id, profundidad);

--
-- Name: fot_foto fot_foto_actualizado_en; Type: TRIGGER; Schema: operacional; Owner: -
--

CREATE TRIGGER fot_foto_actualizado_en BEFORE UPDATE ON operacional.fot_foto FOR EACH ROW EXECUTE FUNCTION operacional.fot_foto_tocar();

--
-- Name: empleado empleado_cargo_fk; Type: FK CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.empleado
    ADD CONSTRAINT empleado_cargo_fk FOREIGN KEY (cargo_id) REFERENCES base.cargo(id) ON DELETE SET NULL;

--
-- Name: empleado empleado_empresa_fk; Type: FK CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.empleado
    ADD CONSTRAINT empleado_empresa_fk FOREIGN KEY (empresa_id) REFERENCES base.empresa(id) ON DELETE SET NULL;

--
-- Name: usuario_rol rol_fk; Type: FK CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario_rol
    ADD CONSTRAINT rol_fk FOREIGN KEY (id_rol) REFERENCES base.rol(id) ON DELETE CASCADE;

--
-- Name: usuario usuario_empleado_fk; Type: FK CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario
    ADD CONSTRAINT usuario_empleado_fk FOREIGN KEY (empleado_id) REFERENCES base.empleado(id) ON DELETE SET NULL;

--
-- Name: usuario_rol usuario_fk; Type: FK CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario_rol
    ADD CONSTRAINT usuario_fk FOREIGN KEY (id_usuario) REFERENCES base.usuario(id) ON DELETE CASCADE;

--
-- Name: crt_densidad crt_densidad_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.crt_densidad
    ADD CONSTRAINT crt_densidad_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;

--
-- Name: crt_muestra crt_muestra_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.crt_muestra
    ADD CONSTRAINT crt_muestra_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;

--
-- Name: dns_avance dns_avance_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.dns_avance
    ADD CONSTRAINT dns_avance_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;

--
-- Name: fot_avance fot_avance_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.fot_avance
    ADD CONSTRAINT fot_avance_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;

--
-- Name: fot_foto fot_foto_sondaje_id_fkey; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.fot_foto
    ADD CONSTRAINT fot_foto_sondaje_id_fkey FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id);

--
-- Name: log_avance log_avance_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.log_avance
    ADD CONSTRAINT log_avance_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;

--
-- Name: log_recepcion_caja log_recepcion_caja_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.log_recepcion_caja
    ADD CONSTRAINT log_recepcion_caja_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;

--
-- Name: mst_avance mst_avance_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.mst_avance
    ADD CONSTRAINT mst_avance_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;

--
-- Name: prf_avance prf_avance_maquina_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_avance
    ADD CONSTRAINT prf_avance_maquina_fk FOREIGN KEY (maquina_id) REFERENCES operacional.prf_maquina(id) ON DELETE SET NULL;

--
-- Name: prf_avance_personal prf_avance_personal_avance_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_avance_personal
    ADD CONSTRAINT prf_avance_personal_avance_fk FOREIGN KEY (avance_id) REFERENCES operacional.prf_avance(id) ON DELETE CASCADE;

--
-- Name: prf_avance_personal prf_avance_personal_perforista_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_avance_personal
    ADD CONSTRAINT prf_avance_personal_perforista_fk FOREIGN KEY (perforista_id) REFERENCES base.empleado(id) ON DELETE SET NULL;

--
-- Name: prf_avance prf_avance_plataforma_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_avance
    ADD CONSTRAINT prf_avance_plataforma_fk FOREIGN KEY (plataforma_id) REFERENCES operacional.prf_plataforma(id) ON DELETE SET NULL;

--
-- Name: prf_avance prf_avance_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_avance
    ADD CONSTRAINT prf_avance_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;

--
-- Name: prf_proyecto_maquina prf_proymaq_maquina_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_proyecto_maquina
    ADD CONSTRAINT prf_proymaq_maquina_fk FOREIGN KEY (maquina_id) REFERENCES operacional.prf_maquina(id) ON DELETE RESTRICT;

--
-- Name: prf_proyecto_maquina prf_proymaq_proyecto_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_proyecto_maquina
    ADD CONSTRAINT prf_proymaq_proyecto_fk FOREIGN KEY (proyecto_id) REFERENCES base.proyecto(id) ON DELETE RESTRICT;

--
-- Name: prf_survey prf_survey_medicion_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey
    ADD CONSTRAINT prf_survey_medicion_fk FOREIGN KEY (medicion_id) REFERENCES operacional.prf_survey_medicion(id) ON DELETE SET NULL;

--
-- Name: prf_survey_medicion prf_survey_medicion_maquina_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey_medicion
    ADD CONSTRAINT prf_survey_medicion_maquina_fk FOREIGN KEY (maquina_id) REFERENCES operacional.prf_maquina(id) ON DELETE SET NULL;

--
-- Name: prf_survey_medicion prf_survey_medicion_perforista_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey_medicion
    ADD CONSTRAINT prf_survey_medicion_perforista_fk FOREIGN KEY (perforista_id) REFERENCES base.empleado(id) ON DELETE SET NULL;

--
-- Name: prf_survey_medicion prf_survey_medicion_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey_medicion
    ADD CONSTRAINT prf_survey_medicion_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE CASCADE;

--
-- PostgreSQL database dump complete
--

-- ===== 4. Catálogos mínimos: roles y cargos =====
INSERT INTO base.cargo (id, numero_interno, codigo, nombre, area, estado) VALUES ('6bdbbbf5-c059-483c-827c-9f730df2b387', 1, 'PERFORISTA', 'Perforista', 'perforacion', true) ON CONFLICT DO NOTHING;
INSERT INTO base.cargo (id, numero_interno, codigo, nombre, area, estado) VALUES ('5ea0118c-fa47-4907-987f-f5a9ae45cd40', 2, 'AYUDANTE_PERF', 'Ayudante de perforación', 'perforacion', true) ON CONFLICT DO NOTHING;
INSERT INTO base.cargo (id, numero_interno, codigo, nombre, area, estado) VALUES ('f622fef3-fe85-49d6-b733-637df5984364', 3, 'SUPERVISOR_PERF', 'Supervisor de perforación', 'perforacion', true) ON CONFLICT DO NOTHING;
INSERT INTO base.cargo (id, numero_interno, codigo, nombre, area, estado) VALUES ('c733f2f1-a2a5-4c4c-a96f-fb2c58067d22', 4, 'GEOLOGO', 'Geólogo', 'geologia', true) ON CONFLICT DO NOTHING;
INSERT INTO base.cargo (id, numero_interno, codigo, nombre, area, estado) VALUES ('aa76c754-42f2-48d5-821a-8ffe1d9ab611', 5, 'AYUDANTE_GEO', 'Ayudante de geología', 'geologia', true) ON CONFLICT DO NOTHING;
INSERT INTO base.cargo (id, numero_interno, codigo, nombre, area, estado) VALUES ('98963ba9-8405-4879-ac42-e09a6038255c', 6, 'JEFE_GEO', 'Jefe de geología', 'geologia', true) ON CONFLICT DO NOTHING;
INSERT INTO base.cargo (id, numero_interno, codigo, nombre, area, estado) VALUES ('189deb73-a88c-4ae7-896c-64e9e2824fed', 7, 'TECNICO_LAB', 'Técnico de laboratorio', 'laboratorio', true) ON CONFLICT DO NOTHING;
INSERT INTO base.cargo (id, numero_interno, codigo, nombre, area, estado) VALUES ('6ae5e805-e860-4411-824f-fd1615a9f024', 8, 'ADMINISTRATIVO', 'Administrativo', 'administracion', true) ON CONFLICT DO NOTHING;
INSERT INTO base.rol (id, numero_interno, codigo, nombre) VALUES ('7d100803-3b71-4fc9-ab7b-fa245b4ca810', 1, 'ADMIN', 'Administrador del Sistema') ON CONFLICT DO NOTHING;
INSERT INTO base.rol (id, numero_interno, codigo, nombre) VALUES ('c7bc9124-1560-48c3-bb6c-e146e440aea0', 2, 'GEOLOGO', 'Geólogo Senior') ON CONFLICT DO NOTHING;
INSERT INTO base.rol (id, numero_interno, codigo, nombre) VALUES ('46f519e4-6ec7-4e89-adce-28b1ab6b884a', 4, 'VISITANTE', 'Usuario de Solo Lectura (Clientes/Gerencia)') ON CONFLICT DO NOTHING;
INSERT INTO base.rol (id, numero_interno, codigo, nombre) VALUES ('37f8d1d6-9cd8-470b-b282-ea528fa30726', 3, 'ASISTENTE_GEOLOGIA', 'Geólogo de Campo / Capturador de Datos') ON CONFLICT DO NOTHING;

COMMIT;
