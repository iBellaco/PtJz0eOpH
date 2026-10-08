# Coach 1.1.10.225 — código 941

## Corrección

La confirmación de un cambio de rol restablecía la carga únicamente en éxito. Un conflicto con otra solicitud o un error dejaba los botones desactivados. Ahora las rutas de error liberan la confirmación y muestran el motivo. Si la operación está pendiente se abre su detalle y no se anuncia que el rol haya cambiado.

Usuario incluye Solicitudes y la gestión de una cuenta incluye el mismo acceso. El detalle muestra la operación, cuenta cuando sus permisos permiten consultarla, rol solicitado, fecha y estado; escucha los cambios de estado y permite cerrar siempre. Los datos privados de pago y los identificadores internos no se muestran. Un estado guardado sin confirmación en vivo se identifica expresamente. Los datos de rol y Premium de la cuenta gestionada se actualizan al recibir la confirmación remota.

La espera del resultado en primer plano pasa de 45 a 5 segundos; la sesión y el envío también tienen espera acotada. Cerrar el detalle no cancela ni repite la operación. La comparación de solicitudes conserva el comportamiento idempotente, incluyendo mapas y números serializados; otra operación no sobrescribe una que todavía esté pendiente. No se cambian permisos, precios ni saldos desde el cliente.

El procesador gratuito se ejecutó durante el diagnóstico (37813480794) y terminó dos solicitudes, sin fallos ni solicitudes omitidas. No se identifica en este reporte a sus titulares ni se afirma que la programación garantice una espera de cinco minutos: las ejecuciones programadas pueden retrasarse. No se activa ningún plan de pago.

## Archivos

- app/build.gradle.kts: versión 225/941.
- EconomyServiceClient.kt y EconomyRequestPolicy.kt: espera acotada, conflicto tipado y comparación de reintentos.
- EconomyPendingStatus.kt, AuthScreen.kt: acceso y detalle en vivo, en español y portugués.
- UserManagementActions.kt, UserDetailManagementDialog.kt: error desbloqueado y acceso desde la cuenta; roles sincronizados.
- translations_ui_pt.json: textos portugueses.
- EconomyRequestPolicyTest.kt, RuntimeVisibilityTest.kt y build-apk.yml: regresiones obligatorias de comparación, estados y confirmación.

## Verificación

Pendientes las comprobaciones obligatorias de compilación, pantallas en ambos idiomas, instalación, R8, firma persistente y descarga de los dos ZIP. No se entrega ni fusiona hasta que hayan terminado correctamente.

## Resumen para testers

Coach 1.1.10.225: abrir Usuario → Solicitudes y revisar operación, cuenta y estado. Comprobar que un error al cambiar de rol habilite de nuevo los botones y que una solicitud en espera abra su detalle. Cerrar el aviso no debe repetir la operación ni anunciar éxito. Revisar español y portugués.

## Commit copiable

fix: mostrar solicitudes pendientes y desbloquear la confirmación de roles en Coach
