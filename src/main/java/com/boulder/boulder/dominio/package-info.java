/**
 * Reglas de dominio puras, sin dependencias de Spring ni de la base de datos.
 *
 * <p>Por ejemplo, {@link com.boulder.boulder.dominio.CodigoSondaje} reconoce un
 * mismo sondaje escrito de formas distintas en los reportes de campo. Al no
 * depender de nada, se prueban con pruebas unitarias simples.</p>
 */
package com.boulder.boulder.dominio;
