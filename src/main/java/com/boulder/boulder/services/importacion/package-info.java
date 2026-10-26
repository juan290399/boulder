/**
 * Carga masiva desde las plantillas oficiales de Excel con Apache POI.
 *
 * <p>Cada plantilla es una implementación de {@code ImportadorPlantilla} sobre
 * {@code ImportadorBase}: las columnas se buscan por nombre de cabecera, cada
 * fila se valida contra los catálogos y la carga se simula antes de aplicarse en
 * una sola transacción.</p>
 */
package com.boulder.boulder.services.importacion;
