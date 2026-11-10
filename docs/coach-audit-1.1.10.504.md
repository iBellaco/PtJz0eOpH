# Coach 1.1.10.504 — Aislamiento de la prueba de aprobación de patrocinios

## Versión

- Versión de producción: `1.1.10.504`, build `1220`.
- Se conserva íntegra la corrección de aprobación de `1.1.10.503`.

## Error identificado

- Ejecución fallida: `42876660007`.
- Trabajo: `Solicitudes, esencias y moderación con las reglas reales`.
- Paso: `Comprobar envíos, moderación e idempotencia en emulador aislado`.
- Error: `EconomyError: El costo ha cambiado. Revisa el total antes de confirmar.`, en `server/economy/service.mjs:302`, invocado por `server/economy/test/emulator.integration.mjs:155`.
- La regresión de aprobación pasó en esa ejecución. Sin embargo, dejó un anuncio activo en el emulador compartido. La siguiente suite esperaba un escenario sin anuncios y su cotización fija dejó de coincidir con el cálculo de ocupación.
- La verificación local anterior ejecutó las suites en otro orden y no detectó esa dependencia.

## Corrección dirigida

- Limpiar los datos del emulador aislado al finalizar `sponsor-review.test.mjs`, también cuando falla una aserción, y liberar siempre los clientes.
- No modificar precios, reglas de producción, cobros ni expectativas de seguridad para hacer pasar la prueba.
- Ejecutar la cadena completa en el mismo orden que la ejecución fallida.

## Archivos

- `tests/firestore/sponsor-review.test.mjs`
- `app/build.gradle.kts`
- Este documento.

## Comprobaciones

- Reproducción local antes del cambio: misma cadena y mismo error de costo en `emulator.integration.mjs:155`.
- Pruebas unitarias de economía: 52 correctas, cero fallos.
- `node --check tests/firestore/sponsor-review.test.mjs` y `git diff --check`: correctos.
- Cadena completa en el orden de CI (`rules`, `economy-submission`, `sponsor-review`, integración de economía): salida 0; 65 escenarios de reglas, envíos, aprobación persistente y 31 casos de integración correctos.
- No se modifican datos reales: la limpieza se limita al proyecto `demo-coach-tests` del emulador en `127.0.0.1:8088`.
- APK release nueva pendiente de la compilación autorizada; no se dispone localmente de la firma persistente de producción.
- Sin fusión en la rama principal ni monitoreo continuo.
