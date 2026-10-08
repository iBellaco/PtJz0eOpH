# Coach 1.1.10.226 — código 942

## Causa y corrección

Al abrir la revisión de pagos se enviaba CLEANUP a la solicitud privada del administrador. Una limpieza automática ocupaba la misma ranura que el cambio de rol. Mostrar la solicitud en la versión 225 resolvió el indicador trabado, pero no esa interferencia ni la dependencia del horario del procesador. Se retira el envío automático del cliente: la conservación sigue a cargo del mantenimiento autorizado existente. Las reglas rechazan nuevas solicitudes CLEANUP del cliente y el procesador conserva compatibilidad para terminar las antiguas.

El cambio de rol ahora es una transacción directa del perfil, autorizada exclusivamente por el claim firmado de administrador. Confirma únicamente después del commit. La asignación de Gratis retira el plazo y plan Premium; la de Premium conserva un plazo activo y concede treinta días si venció. Las reglas limitan la concesión al plazo de treinta días con tolerancia de cinco minutos para el reloj del dispositivo, exigen marcas de tiempo del servidor y autor firmado, validan el rol y la suspensión, y prohíben cambiar saldos, membresías o comprobantes. Se bloquean cuentas purgadas o en proceso de eliminación. El operador suspendido no puede cambiar roles. No se asigna administración desde un perfil ni se alteran claims desde la aplicación.

La consulta del servidor protege el perfil de una orden ROLE anterior que aún quede en espera: su fecha original no puede sobrescribir una modificación más reciente. Un resultado ya registrado sigue siendo idempotente. La suspensión usa la misma transacción y revoca el token de sesión del perfil afectado.

El canje y su revisión son exclusivos del administrador autenticado por claim firmado. Se oculta el acceso a creadores de todos los niveles, streamers, moderadores y demás roles, incluso con un rol secundario o un marco. Los perfiles que dicen admin sin claim tampoco habilitan el canje. Las reglas limitan las lecturas de canjes y su envío; el servicio vuelve a exigir el claim antes de descontar esencias. Compras, pagos y comprobantes permanecen exclusivamente en el servicio. No se activa ningún servicio de pago.

## Archivos principales

- AdminRoleRepository.kt: política y transacción de rol con espera acotada.
- UserManagementActions.kt y UserDetailManagementDialog.kt: confirmación, errores y suspensión.
- EssenceEconomyRepository.kt y OrangeEssenceRedemptionDialog.kt: retiro del mantenimiento automático y revisión exclusiva.
- RolePanelAccess.kt y ModeratorRequestsDialog.kt: acceso de canje únicamente con permiso firmado.
- firestore.rules: validación de roles, conservación de saldos y restricciones de canje.
- server/economy/policy.mjs, service.mjs y queue.mjs: canje administrativo y protección de órdenes antiguas.
- Pruebas de política, reglas, transacciones, lógica y pantallas; textos portugueses y versión 226/942.

## Verificación registrada antes de compilar

43 pruebas de política aprobadas; 55 escenarios de reglas y 21 pruebas de economía transaccional aprobados en emuladores aislados. Incluyen cambio a Gratis con una limpieza antigua pendiente, concesión y conservación de Premium, suspensión y recuperación, imposibilidad de editar saldos junto al rol, rechazo de administración sin claim, prohibición de crear CLEANUP y rechazo de canje por otros roles. La publicación requiere además las comprobaciones obligatorias del APK, pantallas, instalación, firma persistente, R8, activación de permisos y los dos ZIP descargables; sus resultados quedan en la ejecución de esta versión y la solicitud de cambios.

## Resumen para testers

Coach 1.1.10.226: comprobar que abrir historial no genere una solicitud que bloquee el cambio de rol. Asignar Gratis y Premium, verificar la confirmación real y la conservación del saldo. El canje solo debe aparecer para el administrador. Revisar creadores, moderador, streamer y los demás roles en español y portugués.

## Commit copiable

fix: confirmar cambios de rol sin bloqueo y reservar el canje al administrador en Coach
