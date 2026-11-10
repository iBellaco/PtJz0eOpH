# Coach 1.1.10.503 — Corrección de la aprobación persistente de anuncios

## Versión y alcance

- Versión: `1.1.10.503`, build `1219`.
- Alcance exclusivo: desbloquear la transición válida de aprobación de patrocinio y cubrirla con una regresión reproducible.

## Causa reproducida

`SponsorReviewRepository.resolve` escribe `status: APROBADA`, `isApproved: true`
y `isEnabled: true` junto con la publicación pública en una transacción.
La regla de `pending_sponsor_ads` admitía cambios de `status` únicamente a
`REJECTED`. Por ello rechazaba toda la transacción de aprobación, incluida la
publicación. El documento seguía pendiente al volver a consultarlo.

La prueba anterior omitía `status` en su escritura de aprobación y no reproducía
la operación real. Al agregar ese campo, la prueba falló con
`permission-denied` en la regla de actualización, antes de modificar las reglas.
Este defecto existe también en 1.1.10.502; no depende de la antigüedad de la captura.

## Cambio

- Se permite `APROBADA` únicamente con `isApproved` e `isEnabled` verdaderos.
- Se conservan la autorización existente, los campos permitidos y las restricciones sobre anuncios rechazados o reembolsados.
- La prueba incluye la escritura exacta del estado, una segunda consulta sin caché desde otra sesión, la exclusión de pendientes y la lectura pública con las condiciones de visibilidad de Inicio.
- Se verifica la aprobación repetida sin extender la vigencia y se rechazan estados incoherentes o arbitrarios.
- No se modifican precios, cobros, contenido, interfaz ni funcionalidades ajenas.

## Archivos

- `firestore.rules`
- `tests/firestore/sponsor-review.test.mjs`
- `app/build.gradle.kts`
- Este documento.

## Verificación y entrega

- Regresión antes del arreglo: fallo reproducido con `permission-denied`.
- Regresión con el arreglo: aprobación persistente y publicación legible por otra sesión correctas en emulador aislado.
- Suite de permisos: 65 escenarios correctos.
- Suite de economía: 31 escenarios correctos en emuladores aislados, incluida la aprobación desde la cola con un solo cargo.
- Pruebas Android `SponsorReviewPolicyTest` y `SponsorNoticeMergePolicyTest`: 8 pruebas correctas; compilación debug correcta con SDK 36 y JDK 21.
- `node --check tests/firestore/sponsor-review.test.mjs` y `git diff --check`: correctos.
- No se han realizado operaciones sobre anuncios ni cuentas reales.
- La corrección requiere publicar las reglas mediante el proceso de entrega autorizado; el push de código por sí solo no cambia las reglas activas.
- No se entrega el instalador 1.1.10.502 como si contuviera este arreglo. La APK firmada/ofuscada nueva queda pendiente de la compilación de entrega; este entorno no dispone de la clave de producción.
- No se fusiona la rama principal ni se realiza monitoreo continuo.
