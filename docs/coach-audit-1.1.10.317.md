# Coach 1.1.10.317 (build 1033)

## Rendimiento del Draft, actividad y Tier List

- Se redujo la espera del escaneo de baneos a 300 ms y se conserva únicamente el fotograma más reciente para publicar antes la detección.
- El monitoreo muestra la duración exacta desde el último cambio de valores.
- La actividad de usuarios conectados permite filtrar por rol, copiar un usuario, copiar la lista completa y abrir la gestión tocando una cuenta.
- Se eliminó de Tier List el distintivo de variación en puntos porcentuales que aparecía como «pp».
- El icono de subida o bajada de categoría queda asociado al parche y se conserva en la caché hasta que cambia el parche.

## Verificaciones

- `git diff --check`.
- Pruebas unitarias y compilación release mediante GitHub Actions.
