# Coach 1.1.10.606 · Auditoría portuguesa resistente a límites externos

## Corrección

- La auditoría portuguesa conserva Maven Central como origen principal y usa
  un espejo de respaldo cuando el origen responde con límite de solicitudes.
- Las descargas incompletas se eliminan antes del siguiente origen para no
  reutilizar archivos dañados.
- Se incrementó la versión visible de Coach a `1.1.10.606` (1322).

## Verificación

- El fallo remoto se identificó como HTTP 429 durante la descarga de las
  dependencias de auditoría, después de que todas las comprobaciones de
  catálogo, traducciones y builds hubieran pasado.
