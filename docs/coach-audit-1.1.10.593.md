# Coach 1.1.10.593 (1309) · Validación única por revisión

Se corrigió la configuración de validación para evitar que una misma revisión
de `coach-validacion` genere dos ejecuciones completas, una por `push` y otra
por `pull_request`.

## Cambios

- `Build and Release APK` conserva el `push` automático únicamente para
  `main`.
- `coach-validacion` se valida mediante la solicitud de cambios hacia `main`,
  con una sola ejecución por actualización de la solicitud.
- `Validación selectiva de correcciones de Coach` conserva el `push` de
  `fix-error` y deja de duplicarlo con un `pull_request` al mismo destino.
- Se actualizó la versión visible de Coach a `1.1.10.593` (1309).

## Verificaciones

- Se revisaron los disparadores YAML de los dos flujos modificados.
- Se confirmó que `coach-validacion` ya no está incluido en `push` dentro de
  `Build and Release APK`.
- Se confirmó que `fix-error` tiene un único disparador automático por `push`.
- La compilación y las pruebas de aplicación quedan para la validación de
  producción de esta revisión.
