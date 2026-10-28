--
-- PostgreSQL database dump
--

\restrict AJ169OvCYDWLUeaxaUST1GadZ8I2sNb7VawMxs0lAJQcGeasQEBH7dQ56lzhX1l

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: base; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA base;


--
-- Name: geologia; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA geologia;


--
-- Name: logueo; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA logueo;


--
-- Name: operacional; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA operacional;


SET default_tablespace = '';

SET default_table_access_method = heap;

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
-- Name: empresa; Type: TABLE; Schema: base; Owner: -
--

CREATE TABLE base.empresa (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(100) NOT NULL,
    ruc character varying(20) NOT NULL,
    direccion text,
    correo character varying(100),
    telefono character varying(50)
);


--
-- Name: empresa_numero_interno_seq; Type: SEQUENCE; Schema: base; Owner: -
--

CREATE SEQUENCE base.empresa_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: empresa_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: base; Owner: -
--

ALTER SEQUENCE base.empresa_numero_interno_seq OWNED BY base.empresa.numero_interno;


--
-- Name: proyecto; Type: TABLE; Schema: base; Owner: -
--

CREATE TABLE base.proyecto (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    id_empresa uuid NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(50) NOT NULL,
    ubicacion text,
    srid integer DEFAULT 32718
);


--
-- Name: proyecto_numero_interno_seq; Type: SEQUENCE; Schema: base; Owner: -
--

CREATE SEQUENCE base.proyecto_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: proyecto_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: base; Owner: -
--

ALTER SEQUENCE base.proyecto_numero_interno_seq OWNED BY base.proyecto.numero_interno;


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
-- Name: alt_diccionario; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.alt_diccionario (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(4) NOT NULL,
    nombre character varying(100) NOT NULL,
    color_r smallint NOT NULL,
    color_g smallint NOT NULL,
    color_b smallint NOT NULL,
    esta_activo boolean DEFAULT true NOT NULL,
    notas text,
    CONSTRAINT lit_b_chk CHECK (((color_b >= 0) AND (color_b <= 255))),
    CONSTRAINT lit_g_chk CHECK (((color_g >= 0) AND (color_g <= 255))),
    CONSTRAINT lit_r_chk CHECK (((color_r >= 0) AND (color_r <= 255)))
);


--
-- Name: alt_diccionario_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.alt_diccionario_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alt_diccionario_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.alt_diccionario_numero_interno_seq OWNED BY geologia.alt_diccionario.numero_interno;


--
-- Name: alt_estilo; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.alt_estilo (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(4) NOT NULL,
    nombre character varying(100) NOT NULL,
    notas text
);


--
-- Name: alt_estilo_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.alt_estilo_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alt_estilo_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.alt_estilo_numero_interno_seq OWNED BY geologia.alt_estilo.numero_interno;


--
-- Name: alt_intensidad; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.alt_intensidad (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(4) NOT NULL,
    nombre character varying(100) NOT NULL,
    notas text
);


--
-- Name: alt_intensidad_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.alt_intensidad_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alt_intensidad_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.alt_intensidad_numero_interno_seq OWNED BY geologia.alt_intensidad.numero_interno;


--
-- Name: alt_mineral; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.alt_mineral (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(4) NOT NULL,
    nombre character varying(100) NOT NULL,
    esta_activo boolean DEFAULT true NOT NULL,
    notas text
);


--
-- Name: alt_mineral_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.alt_mineral_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alt_mineral_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.alt_mineral_numero_interno_seq OWNED BY geologia.alt_mineral.numero_interno;


--
-- Name: color; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.color (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(2) NOT NULL,
    nombre character varying(50) NOT NULL
);


--
-- Name: color_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.color_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: color_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.color_numero_interno_seq OWNED BY geologia.color.numero_interno;


--
-- Name: est_diccionario; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.est_diccionario (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(100) NOT NULL,
    color_r smallint NOT NULL,
    color_g smallint NOT NULL,
    color_b smallint NOT NULL,
    esta_activo boolean DEFAULT true NOT NULL,
    notas text,
    CONSTRAINT lit_b_chk CHECK (((color_b >= 0) AND (color_b <= 255))),
    CONSTRAINT lit_g_chk CHECK (((color_g >= 0) AND (color_g <= 255))),
    CONSTRAINT lit_r_chk CHECK (((color_r >= 0) AND (color_r <= 255)))
);


--
-- Name: est_diccionario_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.est_diccionario_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: est_diccionario_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.est_diccionario_numero_interno_seq OWNED BY geologia.est_diccionario.numero_interno;


--
-- Name: est_espesor; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.est_espesor (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(4) NOT NULL,
    nombre character varying(100) NOT NULL,
    notas text
);


--
-- Name: est_espesor_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.est_espesor_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: est_espesor_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.est_espesor_numero_interno_seq OWNED BY geologia.est_espesor.numero_interno;


--
-- Name: est_forma; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.est_forma (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(100) NOT NULL,
    notas text
);


--
-- Name: est_forma_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.est_forma_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: est_forma_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.est_forma_numero_interno_seq OWNED BY geologia.est_forma.numero_interno;


--
-- Name: est_relleno; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.est_relleno (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(100) NOT NULL,
    esta_activo boolean DEFAULT true NOT NULL,
    notas text
);


--
-- Name: est_relleno_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.est_relleno_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: est_relleno_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.est_relleno_numero_interno_seq OWNED BY geologia.est_relleno.numero_interno;


--
-- Name: est_rugosidad; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.est_rugosidad (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(100) NOT NULL,
    notas text
);


--
-- Name: est_rugosidad_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.est_rugosidad_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: est_rugosidad_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.est_rugosidad_numero_interno_seq OWNED BY geologia.est_rugosidad.numero_interno;


--
-- Name: est_tipo_venilla; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.est_tipo_venilla (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(5) NOT NULL,
    nombre character varying(100) NOT NULL,
    esta_activo boolean DEFAULT true NOT NULL,
    notas text
);


--
-- Name: est_tipo_venilla_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.est_tipo_venilla_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: est_tipo_venilla_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.est_tipo_venilla_numero_interno_seq OWNED BY geologia.est_tipo_venilla.numero_interno;


--
-- Name: forma_clasto; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.forma_clasto (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(100) NOT NULL
);


--
-- Name: forma_clasto_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.forma_clasto_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: forma_clasto_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.forma_clasto_numero_interno_seq OWNED BY geologia.forma_clasto.numero_interno;


--
-- Name: grupo_litologico; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.grupo_litologico (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(50) NOT NULL
);


--
-- Name: grupo_litologico_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.grupo_litologico_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: grupo_litologico_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.grupo_litologico_numero_interno_seq OWNED BY geologia.grupo_litologico.numero_interno;


--
-- Name: intensidad_color; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.intensidad_color (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(50) NOT NULL
);


--
-- Name: intensidad_color_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.intensidad_color_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: intensidad_color_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.intensidad_color_numero_interno_seq OWNED BY geologia.intensidad_color.numero_interno;


--
-- Name: lit_diccionario; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.lit_diccionario (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    grupo_id uuid NOT NULL,
    codigo character(4) NOT NULL,
    nombre character varying(50) NOT NULL,
    color_r smallint NOT NULL,
    color_g smallint NOT NULL,
    color_b smallint NOT NULL,
    esta_activo boolean DEFAULT true NOT NULL,
    notas text,
    CONSTRAINT lit_b_chk CHECK (((color_b >= 0) AND (color_b <= 255))),
    CONSTRAINT lit_g_chk CHECK (((color_g >= 0) AND (color_g <= 255))),
    CONSTRAINT lit_r_chk CHECK (((color_r >= 0) AND (color_r <= 255)))
);


--
-- Name: lit_diccionario_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.lit_diccionario_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: lit_diccionario_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.lit_diccionario_numero_interno_seq OWNED BY geologia.lit_diccionario.numero_interno;


--
-- Name: lit_textura; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.lit_textura (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(150) NOT NULL,
    esta_activo boolean DEFAULT true NOT NULL,
    notas text
);


--
-- Name: lit_textura_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.lit_textura_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: lit_textura_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.lit_textura_numero_interno_seq OWNED BY geologia.lit_textura.numero_interno;


--
-- Name: min_diccionario; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.min_diccionario (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(50) NOT NULL,
    color_r smallint NOT NULL,
    color_g smallint NOT NULL,
    color_b smallint NOT NULL,
    esta_activo boolean DEFAULT true NOT NULL,
    notas text,
    CONSTRAINT lit_b_chk CHECK (((color_b >= 0) AND (color_b <= 255))),
    CONSTRAINT lit_g_chk CHECK (((color_g >= 0) AND (color_g <= 255))),
    CONSTRAINT lit_r_chk CHECK (((color_r >= 0) AND (color_r <= 255)))
);


--
-- Name: min_diccionario_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.min_diccionario_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: min_diccionario_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.min_diccionario_numero_interno_seq OWNED BY geologia.min_diccionario.numero_interno;


--
-- Name: min_distribucion; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.min_distribucion (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(4) NOT NULL,
    nombre character varying(100) NOT NULL,
    notas text
);


--
-- Name: min_distribucion_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.min_distribucion_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: min_distribucion_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.min_distribucion_numero_interno_seq OWNED BY geologia.min_distribucion.numero_interno;


--
-- Name: min_zona; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.min_zona (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(4) NOT NULL,
    nombre character varying(100) NOT NULL,
    notas text
);


--
-- Name: min_zona_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.min_zona_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: min_zona_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.min_zona_numero_interno_seq OWNED BY geologia.min_zona.numero_interno;


--
-- Name: tamanio_grano; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.tamanio_grano (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(3) NOT NULL,
    nombre character varying(50) NOT NULL,
    notas text
);


--
-- Name: tamanio_grano_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.tamanio_grano_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tamanio_grano_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.tamanio_grano_numero_interno_seq OWNED BY geologia.tamanio_grano.numero_interno;


--
-- Name: tipo_material; Type: TABLE; Schema: geologia; Owner: -
--

CREATE TABLE geologia.tipo_material (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno smallint NOT NULL,
    codigo character(2) NOT NULL,
    nombre character varying(50) NOT NULL
);


--
-- Name: tipo_material_numero_interno_seq; Type: SEQUENCE; Schema: geologia; Owner: -
--

CREATE SEQUENCE geologia.tipo_material_numero_interno_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipo_material_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: geologia; Owner: -
--

ALTER SEQUENCE geologia.tipo_material_numero_interno_seq OWNED BY geologia.tipo_material.numero_interno;


--
-- Name: vw_mm_paleta_alteracion; Type: VIEW; Schema: geologia; Owner: -
--

CREATE VIEW geologia.vw_mm_paleta_alteracion AS
 SELECT codigo,
    nombre,
    (((color_r * 65536) + (color_g * 256)) + color_b) AS mm_color
   FROM geologia.alt_diccionario
  ORDER BY codigo;


--
-- Name: vw_mm_paleta_litologia; Type: VIEW; Schema: geologia; Owner: -
--

CREATE VIEW geologia.vw_mm_paleta_litologia AS
 SELECT codigo,
    nombre,
    ((color_r + (color_g * 256)) + (color_b * 65536)) AS mm_color
   FROM geologia.lit_diccionario;


--
-- Name: vw_mm_paleta_mineralizacion; Type: VIEW; Schema: geologia; Owner: -
--

CREATE VIEW geologia.vw_mm_paleta_mineralizacion AS
 SELECT codigo,
    nombre,
    (((color_r * 65536) + (color_g * 256)) + color_b) AS mm_color
   FROM geologia.min_diccionario
  ORDER BY codigo;


--
-- Name: prf_sondaje; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_sondaje (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    proyecto_id uuid NOT NULL,
    programa_id uuid,
    collar_id uuid NOT NULL,
    plataforma_id uuid,
    maquina_id uuid,
    codigo character varying(25) NOT NULL,
    nombre character varying(25) NOT NULL,
    azimut_setup double precision,
    inclinacion_setup double precision,
    profundidad_actual double precision DEFAULT 0.0,
    profundidad_final double precision,
    medicion_utilizada smallint,
    mediciones_totales smallint,
    fecha_inicio date,
    fecha_fin date,
    campania character varying(25),
    notas text
);


--
-- Name: ialteracion; Type: TABLE; Schema: logueo; Owner: -
--

CREATE TABLE logueo.ialteracion (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid NOT NULL,
    desde numeric(9,2) NOT NULL,
    hasta numeric(9,2) NOT NULL,
    longitud numeric(9,2),
    alt_min_1 uuid,
    alt_estilo_1 uuid,
    alt_pc_1 numeric(7,4),
    vetillas_m_1 smallint,
    alt_min_2 uuid,
    alt_estilo_2 uuid,
    alt_pc_2 numeric(7,4),
    vetillas_m_2 smallint,
    alt_min_3 uuid,
    alt_estilo_3 uuid,
    alt_pc_3 numeric(7,4),
    vetillas_m_3 smallint,
    alt_min_4 uuid,
    alt_estilo_4 uuid,
    alt_pc_4 numeric(7,4),
    vetillas_m_4 smallint,
    alt_min_5 uuid,
    alt_estilo_5 uuid,
    alt_pc_5 numeric(7,4),
    vetillas_m_5 smallint,
    alt_tipo_1 uuid NOT NULL,
    alt_intensidad_1 uuid,
    alt_tipo_2 uuid,
    alt_intensidad_2 uuid,
    comentarios text,
    fecha date,
    geologo character varying(50),
    CONSTRAINT ialt_alt_pc_1_chk CHECK (((alt_pc_1 IS NULL) OR ((alt_pc_1 >= (0)::numeric) AND (alt_pc_1 <= (100)::numeric)))),
    CONSTRAINT ialt_alt_pc_2_chk CHECK (((alt_pc_2 IS NULL) OR ((alt_pc_2 >= (0)::numeric) AND (alt_pc_2 <= (100)::numeric)))),
    CONSTRAINT ialt_alt_pc_3_chk CHECK (((alt_pc_3 IS NULL) OR ((alt_pc_3 >= (0)::numeric) AND (alt_pc_3 <= (100)::numeric)))),
    CONSTRAINT ialt_alt_pc_4_chk CHECK (((alt_pc_4 IS NULL) OR ((alt_pc_4 >= (0)::numeric) AND (alt_pc_4 <= (100)::numeric)))),
    CONSTRAINT ialt_alt_pc_5_chk CHECK (((alt_pc_5 IS NULL) OR ((alt_pc_5 >= (0)::numeric) AND (alt_pc_5 <= (100)::numeric)))),
    CONSTRAINT ialt_longitud_chk CHECK ((longitud = (hasta - desde))),
    CONSTRAINT ialt_rango_chk CHECK ((desde < hasta))
);


--
-- Name: ialteracion_numero_interno_seq; Type: SEQUENCE; Schema: logueo; Owner: -
--

CREATE SEQUENCE logueo.ialteracion_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ialteracion_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: logueo; Owner: -
--

ALTER SEQUENCE logueo.ialteracion_numero_interno_seq OWNED BY logueo.ialteracion.numero_interno;


--
-- Name: iestructural; Type: TABLE; Schema: logueo; Owner: -
--

CREATE TABLE logueo.iestructural (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid NOT NULL,
    desde numeric(9,2),
    hasta numeric(9,2),
    profundidad numeric(9,2) NOT NULL,
    longitud numeric(9,2),
    alpha_angle numeric(9,2),
    beta_angle numeric(9,2) NOT NULL,
    dip numeric(5,2),
    tipo_estructura_id uuid NOT NULL,
    tipo_venilla_id uuid,
    espesor_id uuid,
    forma_id uuid,
    rugosidad_id uuid,
    relleno_1_id uuid,
    relleno_2_id uuid,
    litologia_id uuid NOT NULL,
    orientacion text,
    comentarios text,
    fecha date,
    geologo character varying(100),
    CONSTRAINT iestruc_alpha_angle_chk CHECK (((alpha_angle IS NULL) OR ((alpha_angle >= (0)::numeric) AND (alpha_angle <= (90)::numeric)))),
    CONSTRAINT iestruc_beta_angle_chk CHECK (((beta_angle >= (0)::numeric) AND (beta_angle <= (360)::numeric))),
    CONSTRAINT iestruc_dip_chk CHECK (((dip IS NULL) OR ((dip >= (0)::numeric) AND (dip <= (360)::numeric)))),
    CONSTRAINT iestruc_longitud_chk CHECK (((desde IS NULL) OR (hasta IS NULL) OR (longitud = (hasta - desde)))),
    CONSTRAINT iestruc_profundidad_chk CHECK ((profundidad >= (0)::numeric)),
    CONSTRAINT iestruc_rango_chk CHECK (((desde IS NULL) OR (hasta IS NULL) OR (desde < hasta)))
);


--
-- Name: iestructural_numero_interno_seq; Type: SEQUENCE; Schema: logueo; Owner: -
--

CREATE SEQUENCE logueo.iestructural_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: iestructural_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: logueo; Owner: -
--

ALTER SEQUENCE logueo.iestructural_numero_interno_seq OWNED BY logueo.iestructural.numero_interno;


--
-- Name: ilitologia; Type: TABLE; Schema: logueo; Owner: -
--

CREATE TABLE logueo.ilitologia (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid NOT NULL,
    desde numeric(9,2) NOT NULL,
    hasta numeric(9,2) NOT NULL,
    longitud numeric(9,2) NOT NULL,
    tipo_material_id uuid NOT NULL,
    color_id uuid,
    intensidad_color_id uuid,
    grupo_litologico_id uuid NOT NULL,
    lit_diccionario_id uuid NOT NULL,
    tipo_subroca character varying(50),
    descripcion_roca text,
    tamanio_grano_id uuid,
    lit_textura_id uuid,
    bx_pc_clasto smallint,
    bx_forma_id uuid,
    bx_pc_matriz smallint,
    bx_tipo_matriz_id uuid,
    comentarios text,
    fecha date,
    geologo character varying(50),
    CONSTRAINT ilito_longitud_chk CHECK ((longitud = (hasta - desde))),
    CONSTRAINT ilito_pc_clasto_chk CHECK (((bx_pc_clasto IS NULL) OR ((bx_pc_clasto >= 0) AND (bx_pc_clasto <= 100)))),
    CONSTRAINT ilito_pc_matriz_chk CHECK (((bx_pc_matriz IS NULL) OR ((bx_pc_matriz >= 0) AND (bx_pc_matriz <= 100)))),
    CONSTRAINT ilito_rango_chk CHECK ((desde < hasta))
);


--
-- Name: ilitologia_numero_interno_seq; Type: SEQUENCE; Schema: logueo; Owner: -
--

CREATE SEQUENCE logueo.ilitologia_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ilitologia_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: logueo; Owner: -
--

ALTER SEQUENCE logueo.ilitologia_numero_interno_seq OWNED BY logueo.ilitologia.numero_interno;


--
-- Name: imineralizacion; Type: TABLE; Schema: logueo; Owner: -
--

CREATE TABLE logueo.imineralizacion (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid NOT NULL,
    desde numeric(9,2) NOT NULL,
    hasta numeric(9,2) NOT NULL,
    longitud numeric(9,2) NOT NULL,
    min_zona_id uuid NOT NULL,
    min_1 uuid,
    min_pc_1 numeric(7,4),
    min_dist_1 uuid,
    cu_m_pc_1 double precision,
    cu_pc_1 double precision,
    mo_m_pc_1 double precision,
    mo_pc_1 double precision,
    min_2 uuid,
    min_pc_2 numeric(7,4),
    min_dist_2 uuid,
    cu_m_pc_2 double precision,
    cu_pc_2 double precision,
    mo_m_pc_2 double precision,
    mo_pc_2 double precision,
    min_3 uuid,
    min_pc_3 numeric(7,4),
    min_dist_3 uuid,
    cu_m_pc_3 double precision,
    cu_pc_3 double precision,
    mo_m_pc_3 double precision,
    mo_pc_3 double precision,
    min_4 uuid,
    min_pc_4 numeric(7,4),
    min_dist_4 uuid,
    cu_m_pc_4 double precision,
    cu_pc_4 double precision,
    mo_m_pc_4 double precision,
    mo_pc_4 double precision,
    cu_pct double precision,
    mo_pct double precision,
    descripcion text,
    fecha date,
    geologo character varying(50),
    CONSTRAINT imin_cu_pct_chk CHECK (((cu_pct IS NULL) OR (cu_pct >= (0)::double precision))),
    CONSTRAINT imin_longitud_chk CHECK ((longitud = (hasta - desde))),
    CONSTRAINT imin_min_pc_1_chk CHECK (((min_pc_1 IS NULL) OR ((min_pc_1 >= (0)::numeric) AND (min_pc_1 <= (100)::numeric)))),
    CONSTRAINT imin_min_pc_2_chk CHECK (((min_pc_2 IS NULL) OR ((min_pc_2 >= (0)::numeric) AND (min_pc_2 <= (100)::numeric)))),
    CONSTRAINT imin_min_pc_3_chk CHECK (((min_pc_3 IS NULL) OR ((min_pc_3 >= (0)::numeric) AND (min_pc_3 <= (100)::numeric)))),
    CONSTRAINT imin_min_pc_4_chk CHECK (((min_pc_4 IS NULL) OR ((min_pc_4 >= (0)::numeric) AND (min_pc_4 <= (100)::numeric)))),
    CONSTRAINT imin_mo_pct_chk CHECK (((mo_pct IS NULL) OR (mo_pct >= (0)::double precision))),
    CONSTRAINT imin_rango_chk CHECK ((desde < hasta))
);


--
-- Name: imineralizacion_numero_interno_seq; Type: SEQUENCE; Schema: logueo; Owner: -
--

CREATE SEQUENCE logueo.imineralizacion_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: imineralizacion_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: logueo; Owner: -
--

ALTER SEQUENCE logueo.imineralizacion_numero_interno_seq OWNED BY logueo.imineralizacion.numero_interno;


--
-- Name: iquicklog; Type: TABLE; Schema: logueo; Owner: -
--

CREATE TABLE logueo.iquicklog (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid NOT NULL,
    desde numeric(9,2) NOT NULL,
    hasta numeric(9,2) NOT NULL,
    longitud numeric(9,2) NOT NULL,
    lito_id uuid NOT NULL,
    alt_tipo_1_id uuid,
    alt_intensidad_1_id uuid,
    alt_tipo_2_id uuid,
    alt_intensidad_2_id uuid,
    alt_aso_1_id uuid,
    alt_aso_2_id uuid,
    alt_aso_3_id uuid,
    min_tipo_1_id uuid,
    min_tipo_2_id uuid,
    min_tipo_3_id uuid,
    min_estilo_1_id uuid,
    min_estilo_2_id uuid,
    min_estilo_3_id uuid,
    pc_py numeric(7,4),
    pc_cu numeric(7,4),
    pc_mo numeric(7,4),
    min_zona_id uuid,
    comentarios text,
    fecha date,
    geologo character varying(100),
    CONSTRAINT iquick_longitud_chk CHECK ((longitud = (hasta - desde))),
    CONSTRAINT iquick_pc_cu_chk CHECK (((pc_cu IS NULL) OR ((pc_cu >= (0)::numeric) AND (pc_cu <= (100)::numeric)))),
    CONSTRAINT iquick_pc_mo_chk CHECK (((pc_mo IS NULL) OR ((pc_mo >= (0)::numeric) AND (pc_mo <= (100)::numeric)))),
    CONSTRAINT iquick_pc_py_chk CHECK (((pc_py IS NULL) OR ((pc_py >= (0)::numeric) AND (pc_py <= (100)::numeric)))),
    CONSTRAINT iquick_rango_chk CHECK ((desde < hasta))
);


--
-- Name: iquicklog_numero_interno_seq; Type: SEQUENCE; Schema: logueo; Owner: -
--

CREATE SEQUENCE logueo.iquicklog_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: iquicklog_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: logueo; Owner: -
--

ALTER SEQUENCE logueo.iquicklog_numero_interno_seq OWNED BY logueo.iquicklog.numero_interno;


--
-- Name: vw_mm_alteracion; Type: VIEW; Schema: logueo; Owner: -
--

CREATE VIEW logueo.vw_mm_alteracion AS
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
   FROM (((((((((((((((logueo.ialteracion alt
     JOIN operacional.prf_sondaje s ON ((s.id = alt.sondaje_id)))
     LEFT JOIN geologia.alt_mineral am1 ON ((am1.id = alt.alt_min_1)))
     LEFT JOIN geologia.alt_estilo ae1 ON ((ae1.id = alt.alt_estilo_1)))
     LEFT JOIN geologia.alt_mineral am2 ON ((am2.id = alt.alt_min_2)))
     LEFT JOIN geologia.alt_estilo ae2 ON ((ae2.id = alt.alt_estilo_2)))
     LEFT JOIN geologia.alt_mineral am3 ON ((am3.id = alt.alt_min_3)))
     LEFT JOIN geologia.alt_estilo ae3 ON ((ae3.id = alt.alt_estilo_3)))
     LEFT JOIN geologia.alt_mineral am4 ON ((am4.id = alt.alt_min_4)))
     LEFT JOIN geologia.alt_estilo ae4 ON ((ae4.id = alt.alt_estilo_4)))
     LEFT JOIN geologia.alt_mineral am5 ON ((am5.id = alt.alt_min_5)))
     LEFT JOIN geologia.alt_estilo ae5 ON ((ae5.id = alt.alt_estilo_5)))
     LEFT JOIN geologia.alt_diccionario ad1 ON ((ad1.id = alt.alt_tipo_1)))
     LEFT JOIN geologia.alt_intensidad ai1 ON ((ai1.id = alt.alt_intensidad_1)))
     LEFT JOIN geologia.alt_diccionario ad2 ON ((ad2.id = alt.alt_tipo_2)))
     LEFT JOIN geologia.alt_intensidad ai2 ON ((ai2.id = alt.alt_intensidad_2)))
  ORDER BY s.codigo, alt.desde;


--
-- Name: vw_mm_estructural; Type: VIEW; Schema: logueo; Owner: -
--

CREATE VIEW logueo.vw_mm_estructural AS
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
   FROM (((((((((logueo.iestructural est
     LEFT JOIN operacional.prf_sondaje son ON ((est.sondaje_id = son.id)))
     LEFT JOIN geologia.est_diccionario dic ON ((est.tipo_estructura_id = dic.id)))
     LEFT JOIN geologia.lit_diccionario lit ON ((est.litologia_id = lit.id)))
     LEFT JOIN geologia.est_tipo_venilla ven ON ((est.tipo_venilla_id = ven.id)))
     LEFT JOIN geologia.est_espesor esp ON ((est.espesor_id = esp.id)))
     LEFT JOIN geologia.est_forma for_est ON ((est.forma_id = for_est.id)))
     LEFT JOIN geologia.est_rugosidad rug ON ((est.rugosidad_id = rug.id)))
     LEFT JOIN geologia.est_relleno rel1 ON ((est.relleno_1_id = rel1.id)))
     LEFT JOIN geologia.est_relleno rel2 ON ((est.relleno_2_id = rel2.id)))
  ORDER BY son.codigo, est.desde;


--
-- Name: vw_mm_litologia; Type: VIEW; Schema: logueo; Owner: -
--

CREATE VIEW logueo.vw_mm_litologia AS
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
   FROM ((((((((((logueo.ilitologia lit
     JOIN operacional.prf_sondaje s ON ((s.id = lit.sondaje_id)))
     LEFT JOIN geologia.tipo_material tm ON ((tm.id = lit.tipo_material_id)))
     LEFT JOIN geologia.color c ON ((c.id = lit.color_id)))
     LEFT JOIN geologia.intensidad_color ic ON ((ic.id = lit.intensidad_color_id)))
     LEFT JOIN geologia.grupo_litologico gl ON ((gl.id = lit.grupo_litologico_id)))
     LEFT JOIN geologia.lit_diccionario ld ON ((ld.id = lit.lit_diccionario_id)))
     LEFT JOIN geologia.tamanio_grano tg ON ((tg.id = lit.tamanio_grano_id)))
     LEFT JOIN geologia.lit_textura txt ON ((txt.id = lit.lit_textura_id)))
     LEFT JOIN geologia.forma_clasto fc ON ((fc.id = lit.bx_forma_id)))
     LEFT JOIN geologia.tamanio_grano tgm ON ((tgm.id = lit.bx_tipo_matriz_id)))
  ORDER BY s.codigo, lit.desde;


--
-- Name: vw_mm_mineralizacion; Type: VIEW; Schema: logueo; Owner: -
--

CREATE VIEW logueo.vw_mm_mineralizacion AS
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
   FROM ((((((((((logueo.imineralizacion im
     JOIN operacional.prf_sondaje s ON ((s.id = im.sondaje_id)))
     LEFT JOIN geologia.min_zona mz ON ((mz.id = im.min_zona_id)))
     LEFT JOIN geologia.min_diccionario md1 ON ((md1.id = im.min_1)))
     LEFT JOIN geologia.min_distribucion dist1 ON ((dist1.id = im.min_dist_1)))
     LEFT JOIN geologia.min_diccionario md2 ON ((md2.id = im.min_2)))
     LEFT JOIN geologia.min_distribucion dist2 ON ((dist2.id = im.min_dist_2)))
     LEFT JOIN geologia.min_diccionario md3 ON ((md3.id = im.min_3)))
     LEFT JOIN geologia.min_distribucion dist3 ON ((dist3.id = im.min_dist_3)))
     LEFT JOIN geologia.min_diccionario md4 ON ((md4.id = im.min_4)))
     LEFT JOIN geologia.min_distribucion dist4 ON ((dist4.id = im.min_dist_4)))
  ORDER BY s.codigo, im.desde;


--
-- Name: vw_mm_quicklog; Type: VIEW; Schema: logueo; Owner: -
--

CREATE VIEW logueo.vw_mm_quicklog AS
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
   FROM ((((((((((((((((logueo.iquicklog q
     LEFT JOIN operacional.prf_sondaje son ON ((q.sondaje_id = son.id)))
     LEFT JOIN geologia.lit_diccionario lit ON ((q.lito_id = lit.id)))
     LEFT JOIN geologia.alt_diccionario alt1 ON ((q.alt_tipo_1_id = alt1.id)))
     LEFT JOIN geologia.alt_diccionario alt2 ON ((q.alt_tipo_2_id = alt2.id)))
     LEFT JOIN geologia.alt_intensidad int1 ON ((q.alt_intensidad_1_id = int1.id)))
     LEFT JOIN geologia.alt_intensidad int2 ON ((q.alt_intensidad_2_id = int2.id)))
     LEFT JOIN geologia.alt_mineral aso1 ON ((q.alt_aso_1_id = aso1.id)))
     LEFT JOIN geologia.alt_mineral aso2 ON ((q.alt_aso_2_id = aso2.id)))
     LEFT JOIN geologia.alt_mineral aso3 ON ((q.alt_aso_3_id = aso3.id)))
     LEFT JOIN geologia.min_diccionario min1 ON ((q.min_tipo_1_id = min1.id)))
     LEFT JOIN geologia.min_diccionario min2 ON ((q.min_tipo_2_id = min2.id)))
     LEFT JOIN geologia.min_diccionario min3 ON ((q.min_tipo_3_id = min3.id)))
     LEFT JOIN geologia.min_distribucion est1 ON ((q.min_estilo_1_id = est1.id)))
     LEFT JOIN geologia.min_distribucion est2 ON ((q.min_estilo_2_id = est2.id)))
     LEFT JOIN geologia.min_distribucion est3 ON ((q.min_estilo_3_id = est3.id)))
     LEFT JOIN geologia.min_zona zona ON ((q.min_zona_id = zona.id)))
  ORDER BY son.codigo, q.desde;


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
-- Name: prf_collar; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_collar (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(100) NOT NULL,
    este double precision NOT NULL,
    norte double precision NOT NULL,
    elevacion double precision NOT NULL,
    fecha_medicion date,
    campania character varying(50),
    notas text
);


--
-- Name: prf_collar_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_collar_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prf_collar_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_collar_numero_interno_seq OWNED BY operacional.prf_collar.numero_interno;


--
-- Name: prf_maquina; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_maquina (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    codigo character varying(10) NOT NULL,
    nombre character varying(100) NOT NULL,
    tipo character varying(50),
    alcance_pq smallint,
    alcance_hq smallint,
    alcance_nq smallint,
    marca character varying(50),
    motor character varying(50),
    fecha_compra date,
    fecha_llegada date,
    fecha_inicio date,
    estado character varying(30) DEFAULT 'operativa'::character varying,
    notas text
);


--
-- Name: prf_maquina_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_maquina_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prf_maquina_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_maquina_numero_interno_seq OWNED BY operacional.prf_maquina.numero_interno;


--
-- Name: prf_plataforma; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_plataforma (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    proyecto_id uuid NOT NULL,
    codigo character(8) NOT NULL,
    nombre character varying(50) NOT NULL,
    este double precision NOT NULL,
    norte double precision NOT NULL,
    elevacion double precision NOT NULL,
    eje_mayor double precision,
    eje_menor double precision,
    eje_z double precision,
    area double precision,
    numero_pozas smallint DEFAULT 0,
    estado character varying(30),
    tipo character varying(50),
    ubicacion text,
    notas text
);


--
-- Name: prf_plataforma_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_plataforma_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prf_plataforma_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_plataforma_numero_interno_seq OWNED BY operacional.prf_plataforma.numero_interno;


--
-- Name: prf_programa_sondaje; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_programa_sondaje (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    proyecto_id uuid NOT NULL,
    plataforma_id uuid NOT NULL,
    codigo character(15) NOT NULL,
    nombre character varying(100) NOT NULL,
    codigo_original character varying(20) NOT NULL,
    este double precision NOT NULL,
    norte double precision NOT NULL,
    elevacion double precision NOT NULL,
    profundidad double precision NOT NULL,
    azimut double precision NOT NULL,
    inclinacion double precision NOT NULL,
    plano character varying(50),
    punto character varying(50),
    prioridad character varying(30),
    zona character varying(50),
    flanco character varying(50),
    notas text
);


--
-- Name: prf_programa_sondaje_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_programa_sondaje_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prf_programa_sondaje_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_programa_sondaje_numero_interno_seq OWNED BY operacional.prf_programa_sondaje.numero_interno;


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
-- Name: prf_sondaje_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_sondaje_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prf_sondaje_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_sondaje_numero_interno_seq OWNED BY operacional.prf_sondaje.numero_interno;


--
-- Name: prf_survey; Type: TABLE; Schema: operacional; Owner: -
--

CREATE TABLE operacional.prf_survey (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    numero_interno integer NOT NULL,
    sondaje_id uuid NOT NULL,
    azimut double precision NOT NULL,
    inclinacion double precision NOT NULL,
    profundidad double precision NOT NULL,
    medicion_utilizada smallint,
    metodo_medicion character varying(50),
    qc character varying(10),
    medicion_id uuid
);


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
-- Name: prf_survey_numero_interno_seq; Type: SEQUENCE; Schema: operacional; Owner: -
--

CREATE SEQUENCE operacional.prf_survey_numero_interno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prf_survey_numero_interno_seq; Type: SEQUENCE OWNED BY; Schema: operacional; Owner: -
--

ALTER SEQUENCE operacional.prf_survey_numero_interno_seq OWNED BY operacional.prf_survey.numero_interno;


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
-- Name: vw_mm_collar; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_mm_collar AS
 SELECT s.codigo,
    s.nombre,
    c.este,
    c.norte,
    c.elevacion,
    s.profundidad_final
   FROM (operacional.prf_sondaje s
     JOIN operacional.prf_collar c ON ((c.id = s.collar_id)));


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
-- Name: vw_mm_survey; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_mm_survey AS
 SELECT s.codigo,
    s.nombre,
    sv.profundidad,
    sv.azimut,
    sv.inclinacion
   FROM (operacional.prf_sondaje s
     JOIN operacional.prf_survey sv ON ((sv.sondaje_id = s.id)));


--
-- Name: vw_programa_sondaje; Type: VIEW; Schema: operacional; Owner: -
--

CREATE VIEW operacional.vw_programa_sondaje AS
 SELECT ps.id,
    ps.numero_interno,
    p.codigo AS proyecto_codigo,
    p.nombre AS proyecto_nombre,
    pl.codigo AS plataforma_codigo,
    pl.nombre AS plataforma_nombre,
    ps.codigo,
    ps.codigo_original,
    ps.nombre,
    ps.este,
    ps.norte,
    ps.elevacion,
    ps.profundidad,
    ps.azimut,
    ps.inclinacion,
    ps.plano,
    ps.punto,
    ps.prioridad,
    ps.zona,
    ps.flanco,
    ps.notas
   FROM ((operacional.prf_programa_sondaje ps
     JOIN base.proyecto p ON ((p.id = ps.proyecto_id)))
     JOIN operacional.prf_plataforma pl ON ((pl.id = ps.plataforma_id)));


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
-- Name: empresa numero_interno; Type: DEFAULT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.empresa ALTER COLUMN numero_interno SET DEFAULT nextval('base.empresa_numero_interno_seq'::regclass);


--
-- Name: proyecto numero_interno; Type: DEFAULT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.proyecto ALTER COLUMN numero_interno SET DEFAULT nextval('base.proyecto_numero_interno_seq'::regclass);


--
-- Name: rol numero_interno; Type: DEFAULT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.rol ALTER COLUMN numero_interno SET DEFAULT nextval('base.rol_numero_interno_seq'::regclass);


--
-- Name: usuario numero_interno; Type: DEFAULT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.usuario ALTER COLUMN numero_interno SET DEFAULT nextval('base.usuario_numero_interno_seq'::regclass);


--
-- Name: alt_diccionario numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_diccionario ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.alt_diccionario_numero_interno_seq'::regclass);


--
-- Name: alt_estilo numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_estilo ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.alt_estilo_numero_interno_seq'::regclass);


--
-- Name: alt_intensidad numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_intensidad ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.alt_intensidad_numero_interno_seq'::regclass);


--
-- Name: alt_mineral numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_mineral ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.alt_mineral_numero_interno_seq'::regclass);


--
-- Name: color numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.color ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.color_numero_interno_seq'::regclass);


--
-- Name: est_diccionario numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_diccionario ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.est_diccionario_numero_interno_seq'::regclass);


--
-- Name: est_espesor numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_espesor ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.est_espesor_numero_interno_seq'::regclass);


--
-- Name: est_forma numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_forma ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.est_forma_numero_interno_seq'::regclass);


--
-- Name: est_relleno numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_relleno ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.est_relleno_numero_interno_seq'::regclass);


--
-- Name: est_rugosidad numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_rugosidad ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.est_rugosidad_numero_interno_seq'::regclass);


--
-- Name: est_tipo_venilla numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_tipo_venilla ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.est_tipo_venilla_numero_interno_seq'::regclass);


--
-- Name: forma_clasto numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.forma_clasto ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.forma_clasto_numero_interno_seq'::regclass);


--
-- Name: grupo_litologico numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.grupo_litologico ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.grupo_litologico_numero_interno_seq'::regclass);


--
-- Name: intensidad_color numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.intensidad_color ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.intensidad_color_numero_interno_seq'::regclass);


--
-- Name: lit_diccionario numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.lit_diccionario ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.lit_diccionario_numero_interno_seq'::regclass);


--
-- Name: lit_textura numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.lit_textura ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.lit_textura_numero_interno_seq'::regclass);


--
-- Name: min_diccionario numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_diccionario ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.min_diccionario_numero_interno_seq'::regclass);


--
-- Name: min_distribucion numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_distribucion ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.min_distribucion_numero_interno_seq'::regclass);


--
-- Name: min_zona numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_zona ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.min_zona_numero_interno_seq'::regclass);


--
-- Name: tamanio_grano numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.tamanio_grano ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.tamanio_grano_numero_interno_seq'::regclass);


--
-- Name: tipo_material numero_interno; Type: DEFAULT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.tipo_material ALTER COLUMN numero_interno SET DEFAULT nextval('geologia.tipo_material_numero_interno_seq'::regclass);


--
-- Name: ialteracion numero_interno; Type: DEFAULT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion ALTER COLUMN numero_interno SET DEFAULT nextval('logueo.ialteracion_numero_interno_seq'::regclass);


--
-- Name: iestructural numero_interno; Type: DEFAULT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural ALTER COLUMN numero_interno SET DEFAULT nextval('logueo.iestructural_numero_interno_seq'::regclass);


--
-- Name: ilitologia numero_interno; Type: DEFAULT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia ALTER COLUMN numero_interno SET DEFAULT nextval('logueo.ilitologia_numero_interno_seq'::regclass);


--
-- Name: imineralizacion numero_interno; Type: DEFAULT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion ALTER COLUMN numero_interno SET DEFAULT nextval('logueo.imineralizacion_numero_interno_seq'::regclass);


--
-- Name: iquicklog numero_interno; Type: DEFAULT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog ALTER COLUMN numero_interno SET DEFAULT nextval('logueo.iquicklog_numero_interno_seq'::regclass);


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
-- Name: prf_collar numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_collar ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_collar_numero_interno_seq'::regclass);


--
-- Name: prf_maquina numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_maquina ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_maquina_numero_interno_seq'::regclass);


--
-- Name: prf_plataforma numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_plataforma ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_plataforma_numero_interno_seq'::regclass);


--
-- Name: prf_programa_sondaje numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_programa_sondaje ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_programa_sondaje_numero_interno_seq'::regclass);


--
-- Name: prf_proyecto_maquina numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_proyecto_maquina ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_proyecto_maquina_numero_interno_seq'::regclass);


--
-- Name: prf_sondaje numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_sondaje ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_sondaje_numero_interno_seq'::regclass);


--
-- Name: prf_survey numero_interno; Type: DEFAULT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey ALTER COLUMN numero_interno SET DEFAULT nextval('operacional.prf_survey_numero_interno_seq'::regclass);


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
-- Name: empresa empresa_pk; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.empresa
    ADD CONSTRAINT empresa_pk PRIMARY KEY (id);


--
-- Name: proyecto proyecto_pk; Type: CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.proyecto
    ADD CONSTRAINT proyecto_pk PRIMARY KEY (id);


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
-- Name: alt_diccionario alt_diccionario_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_diccionario
    ADD CONSTRAINT alt_diccionario_codigo_uq UNIQUE (codigo);


--
-- Name: alt_diccionario alt_diccionario_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_diccionario
    ADD CONSTRAINT alt_diccionario_pk PRIMARY KEY (id);


--
-- Name: alt_estilo alt_estilo_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_estilo
    ADD CONSTRAINT alt_estilo_codigo_uq UNIQUE (codigo);


--
-- Name: alt_estilo alt_estilo_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_estilo
    ADD CONSTRAINT alt_estilo_pk PRIMARY KEY (id);


--
-- Name: alt_intensidad alt_intensidad_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_intensidad
    ADD CONSTRAINT alt_intensidad_codigo_uq UNIQUE (codigo);


--
-- Name: alt_intensidad alt_intensidad_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_intensidad
    ADD CONSTRAINT alt_intensidad_pk PRIMARY KEY (id);


--
-- Name: alt_mineral alt_mineral_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_mineral
    ADD CONSTRAINT alt_mineral_codigo_uq UNIQUE (codigo);


--
-- Name: alt_mineral alt_mineral_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.alt_mineral
    ADD CONSTRAINT alt_mineral_pk PRIMARY KEY (id);


--
-- Name: color color_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.color
    ADD CONSTRAINT color_codigo_uq UNIQUE (codigo);


--
-- Name: color color_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.color
    ADD CONSTRAINT color_pk PRIMARY KEY (id);


--
-- Name: est_diccionario est_diccionario_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_diccionario
    ADD CONSTRAINT est_diccionario_codigo_uq UNIQUE (codigo);


--
-- Name: est_diccionario est_diccionario_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_diccionario
    ADD CONSTRAINT est_diccionario_pk PRIMARY KEY (id);


--
-- Name: est_espesor est_espesor_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_espesor
    ADD CONSTRAINT est_espesor_codigo_uq UNIQUE (codigo);


--
-- Name: est_espesor est_espesor_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_espesor
    ADD CONSTRAINT est_espesor_pk PRIMARY KEY (id);


--
-- Name: est_forma est_forma_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_forma
    ADD CONSTRAINT est_forma_codigo_uq UNIQUE (codigo);


--
-- Name: est_forma est_forma_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_forma
    ADD CONSTRAINT est_forma_pk PRIMARY KEY (id);


--
-- Name: est_relleno est_relleno_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_relleno
    ADD CONSTRAINT est_relleno_codigo_uq UNIQUE (codigo);


--
-- Name: est_relleno est_relleno_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_relleno
    ADD CONSTRAINT est_relleno_pk PRIMARY KEY (id);


--
-- Name: est_rugosidad est_rugosidad_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_rugosidad
    ADD CONSTRAINT est_rugosidad_codigo_uq UNIQUE (codigo);


--
-- Name: est_rugosidad est_rugosidad_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_rugosidad
    ADD CONSTRAINT est_rugosidad_pk PRIMARY KEY (id);


--
-- Name: est_tipo_venilla est_tipo_venilla_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_tipo_venilla
    ADD CONSTRAINT est_tipo_venilla_codigo_uq UNIQUE (codigo);


--
-- Name: est_tipo_venilla est_tipo_venilla_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.est_tipo_venilla
    ADD CONSTRAINT est_tipo_venilla_pk PRIMARY KEY (id);


--
-- Name: forma_clasto forma_clasto_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.forma_clasto
    ADD CONSTRAINT forma_clasto_codigo_uq UNIQUE (codigo);


--
-- Name: forma_clasto forma_clasto_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.forma_clasto
    ADD CONSTRAINT forma_clasto_pk PRIMARY KEY (id);


--
-- Name: grupo_litologico grupo_litologico_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.grupo_litologico
    ADD CONSTRAINT grupo_litologico_codigo_uq UNIQUE (codigo);


--
-- Name: grupo_litologico grupo_litologico_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.grupo_litologico
    ADD CONSTRAINT grupo_litologico_pk PRIMARY KEY (id);


--
-- Name: intensidad_color intensidad_color_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.intensidad_color
    ADD CONSTRAINT intensidad_color_codigo_uq UNIQUE (codigo);


--
-- Name: intensidad_color intensidad_color_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.intensidad_color
    ADD CONSTRAINT intensidad_color_pk PRIMARY KEY (id);


--
-- Name: lit_diccionario lit_diccionario_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.lit_diccionario
    ADD CONSTRAINT lit_diccionario_codigo_uq UNIQUE (codigo);


--
-- Name: lit_diccionario lit_diccionario_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.lit_diccionario
    ADD CONSTRAINT lit_diccionario_pk PRIMARY KEY (id);


--
-- Name: lit_textura lit_textura_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.lit_textura
    ADD CONSTRAINT lit_textura_pk PRIMARY KEY (id);


--
-- Name: min_diccionario min_diccionario_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_diccionario
    ADD CONSTRAINT min_diccionario_codigo_uq UNIQUE (codigo);


--
-- Name: min_diccionario min_diccionario_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_diccionario
    ADD CONSTRAINT min_diccionario_pk PRIMARY KEY (id);


--
-- Name: min_distribucion min_distribucion_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_distribucion
    ADD CONSTRAINT min_distribucion_codigo_uq UNIQUE (codigo);


--
-- Name: min_distribucion min_distribucion_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_distribucion
    ADD CONSTRAINT min_distribucion_pk PRIMARY KEY (id);


--
-- Name: min_zona min_zona_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_zona
    ADD CONSTRAINT min_zona_codigo_uq UNIQUE (codigo);


--
-- Name: min_zona min_zona_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.min_zona
    ADD CONSTRAINT min_zona_pk PRIMARY KEY (id);


--
-- Name: tamanio_grano tamanio_grano_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.tamanio_grano
    ADD CONSTRAINT tamanio_grano_codigo_uq UNIQUE (codigo);


--
-- Name: tamanio_grano tamanio_grano_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.tamanio_grano
    ADD CONSTRAINT tamanio_grano_pk PRIMARY KEY (id);


--
-- Name: tipo_material tipo_material_codigo_uq; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.tipo_material
    ADD CONSTRAINT tipo_material_codigo_uq UNIQUE (codigo);


--
-- Name: tipo_material tipo_material_pk; Type: CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.tipo_material
    ADD CONSTRAINT tipo_material_pk PRIMARY KEY (id);


--
-- Name: ialteracion ialt_no_overlap; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_no_overlap EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)'::text) WITH &&);


--
-- Name: ialteracion ialt_pk; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_pk PRIMARY KEY (id);


--
-- Name: iestructural iestruc_no_overlap; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_no_overlap EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)'::text) WITH &&) WHERE (((desde IS NOT NULL) AND (hasta IS NOT NULL)));


--
-- Name: iestructural iestruc_pk; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_pk PRIMARY KEY (id);


--
-- Name: ilitologia ilito_no_overlap; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_no_overlap EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)'::text) WITH &&);


--
-- Name: ilitologia ilito_pk; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_pk PRIMARY KEY (id);


--
-- Name: imineralizacion imin_no_overlap; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_no_overlap EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)'::text) WITH &&);


--
-- Name: imineralizacion imin_pk; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_pk PRIMARY KEY (id);


--
-- Name: iquicklog iquick_no_overlap; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_no_overlap EXCLUDE USING gist (sondaje_id WITH =, numrange(desde, hasta, '[)'::text) WITH &&);


--
-- Name: iquicklog iquick_pk; Type: CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_pk PRIMARY KEY (id);


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
-- Name: prf_collar prf_col_codigo_uq; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_collar
    ADD CONSTRAINT prf_col_codigo_uq UNIQUE (codigo);


--
-- Name: prf_collar prf_collar_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_collar
    ADD CONSTRAINT prf_collar_pk PRIMARY KEY (id);


--
-- Name: prf_maquina prf_maq_codigo_uq; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_maquina
    ADD CONSTRAINT prf_maq_codigo_uq UNIQUE (codigo);


--
-- Name: prf_maquina prf_maquina_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_maquina
    ADD CONSTRAINT prf_maquina_pk PRIMARY KEY (id);


--
-- Name: prf_plataforma prf_plat_codigo_uq; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_plataforma
    ADD CONSTRAINT prf_plat_codigo_uq UNIQUE (codigo);


--
-- Name: prf_plataforma prf_plataforma_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_plataforma
    ADD CONSTRAINT prf_plataforma_pk PRIMARY KEY (id);


--
-- Name: prf_programa_sondaje prf_prog_codigo_uq; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_programa_sondaje
    ADD CONSTRAINT prf_prog_codigo_uq UNIQUE (codigo);


--
-- Name: prf_programa_sondaje prf_programa_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_programa_sondaje
    ADD CONSTRAINT prf_programa_pk PRIMARY KEY (id);


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
-- Name: prf_sondaje prf_son_codigo_uq; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_sondaje
    ADD CONSTRAINT prf_son_codigo_uq UNIQUE (codigo);


--
-- Name: prf_sondaje prf_sondaje_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_sondaje
    ADD CONSTRAINT prf_sondaje_pk PRIMARY KEY (id);


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
-- Name: prf_survey prf_survey_pk; Type: CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey
    ADD CONSTRAINT prf_survey_pk PRIMARY KEY (id);


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
-- Name: proyecto empresa_fk; Type: FK CONSTRAINT; Schema: base; Owner: -
--

ALTER TABLE ONLY base.proyecto
    ADD CONSTRAINT empresa_fk FOREIGN KEY (id_empresa) REFERENCES base.empresa(id);


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
-- Name: lit_diccionario lit_grupo_fk; Type: FK CONSTRAINT; Schema: geologia; Owner: -
--

ALTER TABLE ONLY geologia.lit_diccionario
    ADD CONSTRAINT lit_grupo_fk FOREIGN KEY (grupo_id) REFERENCES geologia.grupo_litologico(id);


--
-- Name: ialteracion ialt_alt_estilo_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_estilo_1_fk FOREIGN KEY (alt_estilo_1) REFERENCES geologia.alt_estilo(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_estilo_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_estilo_2_fk FOREIGN KEY (alt_estilo_2) REFERENCES geologia.alt_estilo(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_estilo_3_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_estilo_3_fk FOREIGN KEY (alt_estilo_3) REFERENCES geologia.alt_estilo(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_estilo_4_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_estilo_4_fk FOREIGN KEY (alt_estilo_4) REFERENCES geologia.alt_estilo(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_estilo_5_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_estilo_5_fk FOREIGN KEY (alt_estilo_5) REFERENCES geologia.alt_estilo(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_intensidad_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_intensidad_1_fk FOREIGN KEY (alt_intensidad_1) REFERENCES geologia.alt_intensidad(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_intensidad_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_intensidad_2_fk FOREIGN KEY (alt_intensidad_2) REFERENCES geologia.alt_intensidad(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_min_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_min_1_fk FOREIGN KEY (alt_min_1) REFERENCES geologia.alt_mineral(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_min_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_min_2_fk FOREIGN KEY (alt_min_2) REFERENCES geologia.alt_mineral(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_min_3_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_min_3_fk FOREIGN KEY (alt_min_3) REFERENCES geologia.alt_mineral(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_min_4_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_min_4_fk FOREIGN KEY (alt_min_4) REFERENCES geologia.alt_mineral(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_min_5_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_min_5_fk FOREIGN KEY (alt_min_5) REFERENCES geologia.alt_mineral(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_tipo_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_tipo_1_fk FOREIGN KEY (alt_tipo_1) REFERENCES geologia.alt_diccionario(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_alt_tipo_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_alt_tipo_2_fk FOREIGN KEY (alt_tipo_2) REFERENCES geologia.alt_diccionario(id) ON DELETE SET NULL;


--
-- Name: ialteracion ialt_prf_sondaje_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ialteracion
    ADD CONSTRAINT ialt_prf_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;


--
-- Name: iestructural iestruc_espesor_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_espesor_fk FOREIGN KEY (espesor_id) REFERENCES geologia.est_espesor(id) ON DELETE RESTRICT;


--
-- Name: iestructural iestruc_forma_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_forma_fk FOREIGN KEY (forma_id) REFERENCES geologia.est_forma(id) ON DELETE RESTRICT;


--
-- Name: iestructural iestruc_litologia_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_litologia_fk FOREIGN KEY (litologia_id) REFERENCES geologia.lit_diccionario(id) ON DELETE RESTRICT;


--
-- Name: iestructural iestruc_relleno_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_relleno_1_fk FOREIGN KEY (relleno_1_id) REFERENCES geologia.est_relleno(id) ON DELETE RESTRICT;


--
-- Name: iestructural iestruc_relleno_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_relleno_2_fk FOREIGN KEY (relleno_2_id) REFERENCES geologia.est_relleno(id) ON DELETE RESTRICT;


--
-- Name: iestructural iestruc_rugosidad_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_rugosidad_fk FOREIGN KEY (rugosidad_id) REFERENCES geologia.est_rugosidad(id) ON DELETE RESTRICT;


--
-- Name: iestructural iestruc_tipo_estructura_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_tipo_estructura_fk FOREIGN KEY (tipo_estructura_id) REFERENCES geologia.est_diccionario(id) ON DELETE RESTRICT;


--
-- Name: iestructural iestruc_tipo_venilla_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iestructural
    ADD CONSTRAINT iestruc_tipo_venilla_fk FOREIGN KEY (tipo_venilla_id) REFERENCES geologia.est_tipo_venilla(id) ON DELETE RESTRICT;


--
-- Name: ilitologia ilito_bx_forma_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_bx_forma_fk FOREIGN KEY (bx_forma_id) REFERENCES geologia.forma_clasto(id) ON DELETE SET NULL;


--
-- Name: ilitologia ilito_bx_tipo_matriz_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_bx_tipo_matriz_fk FOREIGN KEY (bx_tipo_matriz_id) REFERENCES geologia.tamanio_grano(id) ON DELETE SET NULL;


--
-- Name: ilitologia ilito_color_dom_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_color_dom_fk FOREIGN KEY (color_id) REFERENCES geologia.color(id) ON DELETE SET NULL;


--
-- Name: ilitologia ilito_color_ton_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_color_ton_fk FOREIGN KEY (intensidad_color_id) REFERENCES geologia.intensidad_color(id) ON DELETE SET NULL;


--
-- Name: ilitologia ilito_grupo_lito_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_grupo_lito_fk FOREIGN KEY (grupo_litologico_id) REFERENCES geologia.grupo_litologico(id) ON DELETE RESTRICT;


--
-- Name: ilitologia ilito_material_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_material_fk FOREIGN KEY (tipo_material_id) REFERENCES geologia.tipo_material(id) ON DELETE RESTRICT;


--
-- Name: ilitologia ilito_prf_sondaje_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_prf_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;


--
-- Name: ilitologia ilito_tamanio_grano_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_tamanio_grano_fk FOREIGN KEY (tamanio_grano_id) REFERENCES geologia.tamanio_grano(id) ON DELETE SET NULL;


--
-- Name: ilitologia ilito_textura_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_textura_fk FOREIGN KEY (lit_textura_id) REFERENCES geologia.lit_textura(id) ON DELETE SET NULL;


--
-- Name: ilitologia ilito_tipo_lito_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.ilitologia
    ADD CONSTRAINT ilito_tipo_lito_fk FOREIGN KEY (lit_diccionario_id) REFERENCES geologia.lit_diccionario(id) ON DELETE RESTRICT;


--
-- Name: imineralizacion imin_min_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_min_1_fk FOREIGN KEY (min_1) REFERENCES geologia.min_diccionario(id) ON DELETE SET NULL;


--
-- Name: imineralizacion imin_min_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_min_2_fk FOREIGN KEY (min_2) REFERENCES geologia.min_diccionario(id) ON DELETE SET NULL;


--
-- Name: imineralizacion imin_min_3_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_min_3_fk FOREIGN KEY (min_3) REFERENCES geologia.min_diccionario(id) ON DELETE SET NULL;


--
-- Name: imineralizacion imin_min_4_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_min_4_fk FOREIGN KEY (min_4) REFERENCES geologia.min_diccionario(id) ON DELETE SET NULL;


--
-- Name: imineralizacion imin_min_dist_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_min_dist_1_fk FOREIGN KEY (min_dist_1) REFERENCES geologia.min_distribucion(id) ON DELETE SET NULL;


--
-- Name: imineralizacion imin_min_dist_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_min_dist_2_fk FOREIGN KEY (min_dist_2) REFERENCES geologia.min_distribucion(id) ON DELETE SET NULL;


--
-- Name: imineralizacion imin_min_dist_3_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_min_dist_3_fk FOREIGN KEY (min_dist_3) REFERENCES geologia.min_distribucion(id) ON DELETE SET NULL;


--
-- Name: imineralizacion imin_min_dist_4_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_min_dist_4_fk FOREIGN KEY (min_dist_4) REFERENCES geologia.min_distribucion(id) ON DELETE SET NULL;


--
-- Name: imineralizacion imin_prf_sondaje_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_prf_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;


--
-- Name: imineralizacion imin_zona_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.imineralizacion
    ADD CONSTRAINT imin_zona_fk FOREIGN KEY (min_zona_id) REFERENCES geologia.min_zona(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_aso_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_aso_1_fk FOREIGN KEY (alt_aso_1_id) REFERENCES geologia.alt_mineral(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_aso_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_aso_2_fk FOREIGN KEY (alt_aso_2_id) REFERENCES geologia.alt_mineral(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_aso_3_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_aso_3_fk FOREIGN KEY (alt_aso_3_id) REFERENCES geologia.alt_mineral(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_estilo_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_estilo_1_fk FOREIGN KEY (min_estilo_1_id) REFERENCES geologia.min_distribucion(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_estilo_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_estilo_2_fk FOREIGN KEY (min_estilo_2_id) REFERENCES geologia.min_distribucion(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_estilo_3_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_estilo_3_fk FOREIGN KEY (min_estilo_3_id) REFERENCES geologia.min_distribucion(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_intensidad_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_intensidad_1_fk FOREIGN KEY (alt_intensidad_1_id) REFERENCES geologia.alt_intensidad(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_intensidad_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_intensidad_2_fk FOREIGN KEY (alt_intensidad_2_id) REFERENCES geologia.alt_intensidad(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_lito_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_lito_fk FOREIGN KEY (lito_id) REFERENCES geologia.lit_diccionario(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_min_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_min_1_fk FOREIGN KEY (min_tipo_1_id) REFERENCES geologia.min_diccionario(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_min_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_min_2_fk FOREIGN KEY (min_tipo_2_id) REFERENCES geologia.min_diccionario(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_min_3_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_min_3_fk FOREIGN KEY (min_tipo_3_id) REFERENCES geologia.min_diccionario(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_prf_sondaje_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_prf_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_tipo_1_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_tipo_1_fk FOREIGN KEY (alt_tipo_1_id) REFERENCES geologia.alt_diccionario(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_tipo_2_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_tipo_2_fk FOREIGN KEY (alt_tipo_2_id) REFERENCES geologia.alt_diccionario(id) ON DELETE RESTRICT;


--
-- Name: iquicklog iquick_zona_fk; Type: FK CONSTRAINT; Schema: logueo; Owner: -
--

ALTER TABLE ONLY logueo.iquicklog
    ADD CONSTRAINT iquick_zona_fk FOREIGN KEY (min_zona_id) REFERENCES geologia.min_zona(id) ON DELETE RESTRICT;


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
-- Name: prf_plataforma prf_plat_proyecto_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_plataforma
    ADD CONSTRAINT prf_plat_proyecto_fk FOREIGN KEY (proyecto_id) REFERENCES base.proyecto(id) ON DELETE RESTRICT;


--
-- Name: prf_programa_sondaje prf_prog_plataforma_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_programa_sondaje
    ADD CONSTRAINT prf_prog_plataforma_fk FOREIGN KEY (plataforma_id) REFERENCES operacional.prf_plataforma(id) ON DELETE RESTRICT;


--
-- Name: prf_programa_sondaje prf_prog_proyecto_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_programa_sondaje
    ADD CONSTRAINT prf_prog_proyecto_fk FOREIGN KEY (proyecto_id) REFERENCES base.proyecto(id) ON DELETE RESTRICT;


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
-- Name: prf_sondaje prf_son_collar_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_sondaje
    ADD CONSTRAINT prf_son_collar_fk FOREIGN KEY (collar_id) REFERENCES operacional.prf_collar(id) ON DELETE SET NULL;


--
-- Name: prf_sondaje prf_son_maquina_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_sondaje
    ADD CONSTRAINT prf_son_maquina_fk FOREIGN KEY (maquina_id) REFERENCES operacional.prf_maquina(id) ON DELETE SET NULL;


--
-- Name: prf_sondaje prf_son_plataforma_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_sondaje
    ADD CONSTRAINT prf_son_plataforma_fk FOREIGN KEY (plataforma_id) REFERENCES operacional.prf_plataforma(id) ON DELETE SET NULL;


--
-- Name: prf_sondaje prf_son_programa_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_sondaje
    ADD CONSTRAINT prf_son_programa_fk FOREIGN KEY (programa_id) REFERENCES operacional.prf_programa_sondaje(id) ON DELETE SET NULL;


--
-- Name: prf_sondaje prf_son_proyecto_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_sondaje
    ADD CONSTRAINT prf_son_proyecto_fk FOREIGN KEY (proyecto_id) REFERENCES base.proyecto(id) ON DELETE RESTRICT;


--
-- Name: prf_survey prf_srv_sondaje_fk; Type: FK CONSTRAINT; Schema: operacional; Owner: -
--

ALTER TABLE ONLY operacional.prf_survey
    ADD CONSTRAINT prf_srv_sondaje_fk FOREIGN KEY (sondaje_id) REFERENCES operacional.prf_sondaje(id) ON DELETE CASCADE;


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

\unrestrict AJ169OvCYDWLUeaxaUST1GadZ8I2sNb7VawMxs0lAJQcGeasQEBH7dQ56lzhX1l

