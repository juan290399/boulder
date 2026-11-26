# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Geólogos y asistentes de geología del campamento de exploración VERONICA
(perforación diamantina). Trabajan desde PCs de escritorio y laptops
conectadas a la red local del campamento; un servidor del propio campamento
aloja la base PostgreSQL. Capturan a diario los partes de turno (perforación,
logueo, almacén, muestreo, desvíos, quicklog, fotos de testigo) y revisan el
avance por sondaje. Gerencia y clientes entran en solo lectura.

Roles: ADMIN, GEOLOGO (geólogo senior, revisa), ASISTENTE_GEOLOGIA (captura)
y VISITANTE (solo lectura).

## Product Purpose

Reemplazar los Excel dispersos con que cada área lleva la campaña de
perforación: una sola captura por dato, reglas de dominio que impiden
intervalos cruzados o metros imposibles, y el reporte diario y de periodo
generados por el sistema en vez de armados a mano. El éxito es que el
campamento deje de mantener los reportes en paralelo.

Es además el Proyecto Final del Curso Integrador I (UTP). En la sustentación
el docente pide cambios en vivo sobre la interfaz (mover, recolorear,
subrayar botones, agregar columnas) y pregunta por el código.

## Operating Context

- Red local del campamento, internet por Starlink; acceso externo solo por
  Tailscale. Sin dependencia de servicios en la nube.
- Datos de una campaña real (sondajes `DDH-VERO-S-…`), importables desde las
  plantillas Excel de cada área.
- Pantallas de trabajo largas: tablas con muchas filas y columnas
  numéricas (metros, profundidades, azimut, inclinación).

## Capabilities and Constraints

- Angular 21 con Bootstrap 5 en el cliente; Spring Boot 4 y PostgreSQL 18 en
  el servidor. Bootstrap se mantiene: sus clases utilitarias son el camino
  más rápido para los cambios en vivo de la sustentación.
- Sistema visual propio en `boulder-app/src/styles.scss` (tokens `--bo-*`
  sobre Bootstrap); los componentes no inventan colores ni espaciados.
- El menú lateral se arma desde `components/navbar/navbar.ts`, no en el HTML.
- Terminología del dominio en español minero: sondaje, plataforma, collar,
  parte, logueo, quicklog, desvío, testigo, caja.

## Brand Commitments

Nombre «Boulder» con el ícono propio de un sondaje cruzando estratos
(`<app-marca>`, también en `public/favicon.svg`) y el
subtítulo «Gestión geológica».

## Evidence on Hand

Datos reales de la campaña VERONICA en la base local (`boulder_db`) y el
respaldo del campamento. No hay testimonios ni métricas de uso que citar.

## Product Principles

1. Un dato se captura una vez; todo lo demás se deriva.
2. Las reglas del dominio se ven en la pantalla antes de que el error llegue
   a la base.
3. Leer rápido una tabla larga pesa más que la decoración.
4. Cada pantalla debe poder modificarse en minutos con clases de Bootstrap.

## Prioridad actual

Revisión pantalla por pantalla con miras a la sustentación de la semana del
25/11/2026: pulido y claridad ante el docente sin romper funciones.
