/**
 * Boulder Drilling: backend de la plataforma de gestión de campañas de perforación
 * diamantina de Minera Enproyec S.A.C.
 *
 * <h2>Arquitectura</h2>
 * <p>Aplicación Spring Boot en capas, con el patrón MVC del lado del servidor y
 * acceso a datos por DAO:</p>
 * <ul>
 *   <li><b>Controlador</b> ({@link com.boulder.boulder.controllers}): recibe la
 *       petición HTTP, valida el formato y delega. No contiene reglas.</li>
 *   <li><b>Servicio</b> ({@link com.boulder.boulder.services}): las reglas de
 *       negocio y las transacciones.</li>
 *   <li><b>DAO</b> ({@link com.boulder.boulder.repositories}): el acceso a
 *       PostgreSQL con Spring Data JPA o JdbcTemplate.</li>
 *   <li><b>Modelo</b> ({@link com.boulder.boulder.entities} y
 *       {@link com.boulder.boulder.dto}): las entidades persistentes y los
 *       objetos que viajan por el API.</li>
 * </ul>
 * <p>La vista es la aplicación Angular ({@code boulder-app}), que consume el API
 * REST con un token JWT.</p>
 *
 * <h2>Principios SOLID aplicados</h2>
 * <ul>
 *   <li><b>Responsabilidad única</b>: el cambio de contraseña propio tiene su
 *       servicio, separado de la administración de cuentas.</li>
 *   <li><b>Abierto/cerrado</b>: una plantilla de carga nueva es otra
 *       implementación de {@code ImportadorPlantilla}; un parte de avance nuevo
 *       hereda {@code AvanceOperativoServiceBase}.</li>
 *   <li><b>Sustitución de Liskov</b>: los siete partes de avance se usan a través
 *       de la misma cabecera {@code AvanceOperativo}.</li>
 *   <li><b>Segregación de interfaces</b>: {@code AlmacenFotos} expone solo
 *       guardar, abrir, existe y borrar.</li>
 *   <li><b>Inversión de dependencias</b>: los servicios reciben sus
 *       dependencias por el constructor y dependen de interfaces, lo que permite
 *       probarlos con dobles de Mockito.</li>
 * </ul>
 */
package com.boulder.boulder;
