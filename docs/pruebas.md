# Plan y resultados de las pruebas de software

## Estrategia

Las pruebas siguen TDD: cada regla nueva entra con un commit `test(…) (rojo)` que
falla y otro `feat(…) (verde)` que la hace pasar. Se combinan cuatro niveles:

| Nivel | Herramienta | Qué cubre | Base de datos |
|---|---|---|---|
| Unitarias | JUnit 5, Mockito, AssertJ | Dominio (`ParteTurno`, `Revision`, `Desviacion`, `CodigoSondaje`), servicios con dobles, generadores de Excel | No |
| Web | MockMvc (`@SpringBootTest`) | Controladores, validación, códigos HTTP y la cadena de seguridad | Sí |
| Integración | `*IT` con PostgreSQL real | Importador con las plantillas oficiales, reportes, partes, permisos, monitoreo | Sí, cada prueba se deshace |
| Frontend | Vitest (Angular) | Guards de autenticación, interceptor del token, creación de componentes | No |

## Ejecución

```bash
./gradlew test jacocoTestReport          # backend con Gradle
./mvnw verify                            # backend con Maven (Surefire + Failsafe)
cd boulder-app && npx ng test --watch=false
```

Las pruebas de integración leen `DB_PASSWORD` de `application-local.properties`;
sin base disponible fallan al levantar el contexto.

## Resultados

| Conjunto | Pruebas | Fallos |
|---|---|---|
| Backend: unitarias | 214 | 0 |
| Backend: web | 21 | 0 |
| Backend: integración | 89 | 0 |
| **Backend total** (40 clases) | **324** | **0** |
| Frontend (19 archivos) | 25 | 0 |

Cobertura del backend (JaCoCo):

| Métrica | Valor |
|---|---|
| Líneas | 67.8 % |
| Ramas | 60.9 % |
| Métodos | 66.8 % |
| Clases | 97.0 % |

El reporte navegable está en [docs/cobertura/](cobertura/index.html).

## Casos representativos

| Regla | Prueba |
|---|---|
| El avance del turno es hasta − desde | `ParteTurnoTest` |
| Una fila del lote de GeoLog rechazada no tumba las demás | `LogueoServiceTest` |
| «VERO S 35_26» se reconoce como `DDH-VERO-S-035-26`; «DDH-35» se rechaza | `CatalogosTest`, `ImportacionIT` |
| El visitante lee pero no escribe; el asistente no borra partes | `PermisosEscrituraTest`, `PermisosApiIT` |
| El reporte de periodo rechaza rangos invertidos, futuros o de más de 92 días | `ReportePeriodoServiceTest` |
| La hoja de quicklog sale ordenada por profundidad y sin relleno de `char(n)` | `HojaQuicklogExcelTest`, `HojaQuicklogIT` |
| Un código que empieza con «=» queda como texto en el Excel | `ReporteDiarioExcelTest`, `ReportePeriodoExcelTest` |
| Sin sesión, una ruta protegida manda al login | `auth.spec.ts` |
