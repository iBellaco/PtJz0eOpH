# Coach 1.1.10.406 — build 1122

## Problema y corrección

La aprobación de anuncios dependía de la caché local, publicaba antes de confirmar
la escritura y enviaba un campo no permitido (`approvedAt`). Se sustituye en la
bandeja unificada por una transacción que lee la solicitud remota, publica el
anuncio y actualiza únicamente los cuatro campos admitidos. Solo se muestra éxito
cuando termina la transacción. Rechazar también espera confirmación y conserva
los otros avisos existentes. Un anuncio reembolsado no puede aprobarse.

La lista escucha las solicitudes en tiempo real. Los indicadores usan una sola
cola compartida; no vuelven a sumar copias locales. El anunciante consulta sus
propios anuncios remotos y ve por separado solicitudes aún pendientes de
procesamiento, evitando confundirlas con una aprobación ya disponible. No se
modifican saldos ni se ejecuta el trabajador real desde este cambio.

## Archivos

- SponsorReviewRepository.kt y SponsorReviewPolicyTest.kt.
- ModeratorRequestsDialog.kt, SponsorCpmPanelDialog.kt, UserPanelNotifications.kt.
- PanelNotificationStore.kt y recursos sponsor_review.xml en español/portugués.
- RuntimeVisibilityTest.kt: ausencia del botón separado eliminado previamente.
- DraftVisionScanner.kt y DraftSlotVisibilityTest.kt: reloj inyectable para probar
  la salida sostenida del draft con la captura existente, sin esperas reales.
- tests/firestore/sponsor-review.test.mjs y run-tests.mjs.
- .github/workflows/fix-error.yml y app/build.gradle.kts.

## Validación

La revisión efd84e79 del décimo slot pasó la ejecución 40629599691. Esta revisión
de patrocinio requiere una nueva ejecución de fix-error: pruebas JVM dirigidas
y transacciones con las reglas reales en emulador. No se declara validación
del flujo en producción ni prueba física en teléfono.

El propietario avisará cuando esté verde; no se mantiene monitoreo continuo.
Solo entonces se integra en coach-validacion. main sigue siendo manual.
