# Auditoría Coach 1.1.10.538

- `versionCode`: `1254`.
- `versionName`: `1.1.10.538`.

## Consolidación de validación en fix-error

- La validación selectiva de `fix-error` se conserva como el único flujo para
  esa rama.
- El flujo antiguo pendiente corresponde al mismo workflow y al mismo grupo de
  concurrencia; no se crea un segundo workflow paralelo.
- La nueva revisión se publica en el mismo flujo y reemplaza la ejecución
  obsoleta mediante la política de concurrencia existente.

## Verificación

- Se revisaron los workflows activos y sus disparadores.
- Se comprobó que `build-apk` queda reservado para `main` y `coach-validacion`.
- Se ejecutaron las comprobaciones de formato, JSON e idiomas antes de publicar.
