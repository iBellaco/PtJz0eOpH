# Auditoría Coach 1.1.10.537

- `versionCode`: `1253`.
- `versionName`: `1.1.10.537`.

## Borrado general de reportes

- El botón general ya no filtra únicamente `SOLVED`: elimina de forma
  confirmada los reportes `READ`, `SOLVED`, `COMPLETED`, `ACCEPTED` y `REJECTED`.
- Los pendientes permanecen para no retirar solicitudes que todavía necesitan
  atención.
- Cada eliminación comprueba el documento canónico del servidor y limpia la
  copia de la bandeja del propietario; después se vuelve a cargar la lista.

## Coordenadas y baneos

- Se retiró el catálogo productivo anterior de coordenadas. El respaldo activo
  y el código contienen únicamente la medición nueva de `motorola edge 30 neo`
  (2174×1080, Android 14/API 34), con los ratios entregados por el propietario.
  También se reconoce su superficie exacta de captura 1920×954, generada por el
  límite de MediaProjection, sin compartirla con otro modelo.
- La captura de evidencia dibuja siempre la fila de baneos con la calibración
  guardada. Una observación adaptativa irregular ya no puede desplazar los cinco
  círculos ni cerrar sus huecos.

## Evidencia visual de campeones

- Al guardar y enviar coordenadas se reutilizan los retratos locales confirmados
  por texto o imagen en los slots aliados, rivales y baneos.
- Los retratos se muestran tanto en el visor como en la imagen adjunta; si un
  recurso no está disponible, el nombre confirmado continúa apareciendo y no se
  inventa una identidad.
- La imagen conserva la captura literal, el nombre de cada slot, la huella
  SHA-256 y la edad del fotograma.

## Verificación

- Se actualizaron las pruebas de elegibilidad del borrado general, perfiles
  exactos y renderizado de retratos/coordenadas.
- `git diff --check`, validación JSON del respaldo y validadores de recursos se
  ejecutan antes de publicar.
- La compilación Android local no pudo ejecutarse porque la imagen disponible no
  incluye el compilador `javac`; la ejecución de tests Android queda para CI con
  JDK y SDK configurados.
