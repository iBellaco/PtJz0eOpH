# Coach 1.1.10.507 — Cierre de solicitudes al resolver y eliminar anuncios

## Corrección

- La aprobación o el rechazo administrativo cierra la solicitud económica original en la misma operación, incluidas las colas antiguas que usaban identificadores diferentes.
- La eliminación busca también solicitudes antiguas por propietario o identificador del anuncio cuando la versión anterior no guardó `queuedRequestId`, y limpia el aviso visible y el registro compartido.
- El botón de eliminación refresca la autorización antes de actuar, por lo que también funciona con una sesión administrativa abierta antes de recibir la autorización.
- El indicador de solicitudes compara todos los identificadores actuales y heredados; un anuncio ya aprobado, rechazado o reembolsado no vuelve a mostrarse como pendiente.
- Se mantienen la eliminación pública, privada y local introducida en 1.1.10.506.

## Versión y verificación

- `1.1.10.507`, build `1223`.

## Verificación

- Pruebas Android dirigidas (`SponsorPendingStateTest`, `SponsorReviewPolicyTest`, `SponsorNoticeMergePolicyTest`): correctas.
- Compilación de pruebas `Debug`: correcta.
- La validación previa de 1.1.10.506 conservó las pruebas de patrocinio, reglas de acceso y economía ya aprobadas.
