# Coach 1.1.10.439 — build 1155

- Lecturas de cargo y devolución anteriores a cualquier escritura de reconciliación en la transacción de rechazo.
- Regresión en emulador con solicitud original en REVIEW: comprobar resolución, recibo y devolución única.
- La lista de solicitudes pendientes excluye operaciones FAILED y COMPLETED; conserva los registros remotos y los pendientes sin confirmar.
- Una operación en cola muestra espera, no un fallo genérico. Un fallo al refrescar anuncios después de confirmar la decisión no convierte el resultado confirmado en error.
- No se eliminaron datos remotos ni se afirma haber reparado operaciones existentes en producción. Los cambios requieren publicación y procesamiento del servicio actualizado.

Archivos: service.mjs, emulator.integration.mjs, EconomyPendingStatus.kt, ModeratorRequestsDialog.kt, app/build.gradle.kts.

Verificación: 46 pruebas unitarias de servicio y 30 casos de integración en emulador aprobados, incluida la regresión de rechazo con cola REVIEW y devolución única. Recursos localizados y git diff --check correctos. Compilación Android y verificación física pendientes.
