/**
 * Monitoreo y mantenimiento desde dentro de la aplicación.
 *
 * <p>Métricas del negocio ({@link com.boulder.boulder.monitoreo.MetricasNegocio}),
 * salud del almacén de fotos, identificador de petición en la bitácora y tareas
 * programadas con cron. Lo que se vigila desde fuera (Prometheus, respaldos,
 * verificación de salud) está en {@code deploy/} y {@code scripts/mantenimiento}.</p>
 */
package com.boulder.boulder.monitoreo;
