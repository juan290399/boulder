---
name: Boulder
description: Plataforma de control operativo de sondajes para el campamento VERONICA
colors:
  brand-500: "#2563eb"
  brand-600: "#1d4ed8"
  brand-400: "#3b82f6"
  brand-soft: "rgba(37, 99, 235, 0.12)"
  canvas: "#f1f5f9"
  surface: "#ffffff"
  surface-2: "#f8fafc"
  border: "#e2e8f0"
  border-strong: "#cbd5e1"
  navy-900: "#081225"
  navy-800: "#0b1730"
  navy-700: "#14213d"
  ink: "#0f172a"
  ink-2: "#475569"
  ink-3: "#94a3b8"
  ink-invert: "#ffffff"
  ink-invert-2: "#cbd5e1"
  ok: "#059669"
  ok-soft: "#d1fae5"
  warn: "#d97706"
  warn-soft: "#fef3c7"
  danger: "#dc2626"
  danger-soft: "#fee2e2"
  info: "#0891b2"
  info-soft: "#cffafe"
  neutral-soft: "#eef2f7"
typography:
  page-title:
    fontFamily: "Inter, Segoe UI, system-ui, sans-serif"
    fontSize: "1.4rem"
    fontWeight: 700
    letterSpacing: "-0.015em"
  panel-title:
    fontFamily: "Inter, Segoe UI, system-ui, sans-serif"
    fontSize: "0.95rem"
    fontWeight: 650
  body:
    fontFamily: "Inter, Segoe UI, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
  label:
    fontFamily: "Inter, Segoe UI, system-ui, sans-serif"
    fontSize: "0.72rem"
    fontWeight: 600
    letterSpacing: "0.06em"
  metric:
    fontFamily: "Inter, Segoe UI, system-ui, sans-serif"
    fontSize: "1.65rem"
    fontWeight: 700
    letterSpacing: "-0.02em"
  mono:
    fontFamily: "JetBrains Mono, Cascadia Mono, Consolas, monospace"
    fontSize: "0.85rem"
rounded:
  sm: "8px"
  md: "12px"
  lg: "18px"
  pill: "999px"
spacing:
  xs: "0.25rem"
  sm: "0.75rem"
  md: "1.25rem"
  lg: "1.5rem"
components:
  button-primary:
    backgroundColor: "{colors.brand-500}"
    textColor: "{colors.ink-invert}"
    rounded: "{rounded.sm}"
  button-primary-hover:
    backgroundColor: "{colors.brand-600}"
  panel:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
  panel-head:
    backgroundColor: "{colors.surface-2}"
    padding: "1rem 1.25rem"
  table-header:
    backgroundColor: "{colors.surface-2}"
    textColor: "{colors.ink-2}"
    typography: "{typography.label}"
    padding: "0.7rem 0.9rem"
  chip:
    backgroundColor: "{colors.neutral-soft}"
    rounded: "{rounded.pill}"
    padding: "0.2rem 0.6rem"
  sidebar:
    backgroundColor: "{colors.navy-900}"
    textColor: "{colors.ink-invert-2}"
    width: "268px"
  sidebar-link-active:
    backgroundColor: "{colors.brand-soft}"
    textColor: "{colors.ink-invert}"
    rounded: "{rounded.sm}"
    height: "44px"
  topbar:
    backgroundColor: "{colors.surface}"
    height: "68px"
---

# Design System: Boulder

## Overview

Herramienta de trabajo (modo Operate) para geólogos en campamento: lienzo
claro y frío, barra lateral azul marino, un solo azul de marca y tablas
densas. La personalidad está en la precisión —números tabulares, códigos de
sondaje en monoespaciada, estados con color semántico— y no en la
decoración. Todo vive en `boulder-app/src/styles.scss`, que reapunta las
variables de Bootstrap 5 a los tokens `--bo-*`: cualquier `.btn`,
`.form-control` o `.table` hereda el sistema sin marcado extra, y las clases
utilitarias de Bootstrap siguen funcionando para los cambios en vivo.

## Colors

### Primary
`brand-500` (#2563eb) es el único azul de acción: botón primario, enlace,
foco de campos y la marca de la barra lateral. `brand-soft` resalta el ítem
activo del menú y el anillo de foco.

### Neutral
Lienzo `canvas` gris azulado; paneles `surface` blancos con cabecera
`surface-2`; bordes `border` y `border-strong` para campos. Tinta en tres
pesos (`ink`, `ink-2`, `ink-3`). La barra lateral usa `navy-900`→`navy-800`
en degradado vertical con texto `ink-invert-2`.

### Estados
`ok`, `warn`, `danger` e `info`, cada uno con su versión `-soft` para fondos
de chips, avisos y barras de avance. El color de estado nunca es el único
portador del significado: va con texto o ícono.

### Named Rules
**Un solo azul.** No se introducen otros azules de marca; el celeste queda
solo como acento de datos (`info`).

## Typography

Una sola pila (Inter con respaldo Segoe UI/system-ui) en toda la aplicación;
JetBrains Mono para códigos de sondaje y valores técnicos.

### Hierarchy
- Título de página 1.4rem/700.
- Título de panel 0.95rem/650.
- Cuerpo y celdas 0.875rem.
- Cabeceras de tabla y rótulos en versalitas: 0.72rem/600, mayúsculas,
  tracking 0.06em, color `ink-2`.
- Métricas 1.65rem/700 con cifras tabulares.

### Named Rules
**Cifras tabulares.** Toda columna numérica usa `.num` (alineada a la
derecha, `tabular-nums`).

## Layout

Armazón de dos columnas: barra lateral fija de 268px y columna principal con
cabecera pegajosa de 68px y lienzo con margen de 1.5rem. Cada pantalla se
arma con `.page-head` (título + acciones a la derecha), `.metric-grid` para
indicadores y `.panel` apilados con 1.25rem entre sí. Las tablas largas van
en `.table-scroll` con la cabecera pegada arriba.

## Elevation & Depth

Una familia de sombras suaves (`shadow-sm` en paneles, `shadow-md`/`lg` para
capas flotantes) y una sombra azul (`shadow-brand`) solo en el hover del
botón primario y en la marca. La jerarquía se marca con bordes y fondos, no
con sombras fuertes.

## Shapes

Tres radios y ni uno más: 8px en botones y campos, 12px en elementos
medianos, 18px en paneles; chips en píldora.

## Components

### Buttons
Clases de Bootstrap (`btn-primary`, `btn-outline-secondary`, `btn-sm`…)
reapuntadas a la marca; peso 500, hover con sombra azul en primario y éxito,
`translateY(1px)` al presionar.

### Chips
`.chip` en píldora con fondo `neutral-soft` y variantes por estado.

### Cards / Containers
`.panel` blanco con borde, radio 18px y `.panel__head` gris claro con título
y acciones.

### Inputs / Fields
`form-control` y `form-select` con radio 8px, borde `border-strong` y foco
con anillo `brand-soft` de 3px. Rótulos `.form-label` 0.8rem/600 en `ink-2`.

### Navigation
Barra lateral oscura con secciones en versalitas y enlaces de 44px de alto;
el activo lleva fondo `brand-soft` y una línea interior izquierda
`brand-400`. Las entradas se definen en `navbar.ts`.

### Data table
`.data-table`: densa, cabecera pegajosa en versalitas, separadores de 1px y
hover de fila en `surface-2`; `.num` para números y `.mono` para códigos.

### Maestro-detalle
Las pantallas de mantenimiento (Usuarios, Empresas, Proyectos, Empleados,
Diccionarios, Sondajes, Plataformas, Máquinas) abren con la lista a todo el
ancho. Elegir una fila o pulsar el botón «Nuevo…» del `page-head` reparte la
pantalla con `.maestro-detalle.con-ficha`: lista a la izquierda y
`<app-ficha>` a la derecha (título, campos, pie fijo con Guardar/Cancelar).
Bajo 1200 px la ficha reemplaza a la lista y ofrece «Volver a la lista». Los
avisos van arriba: `alert-inline--ok` con `role="status"` para éxitos y
`alert-inline` con `role="alert"` para errores, con el texto de
`mensajeDeError`. Los errores del formulario van dentro de la ficha.

### Filas que se despliegan
Una fila que abre un detalle (Avances, Desvíos) lleva en su primera celda un
`.fila-boton` con `aria-expanded`; al abrir, la vista y el foco van al título
del detalle.

### Visor de fotos
`<app-visor>`: diálogo modal con etiqueta, cierre con Esc o con el fondo,
foco atrapado y devuelto a la miniatura.

## Do's and Don'ts

### Do:
- Consumir tokens `--bo-*` o clases de Bootstrap; agregar un token nuevo en
  `styles.scss` si hace falta.
- Mantener las tablas legibles: números alineados a la derecha, códigos en
  monoespaciada, estados con texto.
- Dejar que un cambio en vivo con una clase de Bootstrap funcione (no
  bloquear utilidades con selectores demasiado específicos).

### Don't:
- No inventar colores, radios ni sombras dentro de un componente.
- No usar un segundo azul de marca ni degradados decorativos fuera de la
  barra lateral.
- No reemplazar Bootstrap ni el armazón oscuro/claro sin decisión explícita.
