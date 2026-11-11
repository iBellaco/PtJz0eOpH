# Coach 1.1.10.512 — Borrado de patrocinio y calibración Motorola

## Correcciones

- El borrado de un anuncio patrocinado desde las vistas administrativas ahora
  usa la eliminación atómica del registro privado, la publicación visible, la
  cola de solicitud y el registro local. La autorización sigue exigiendo una
  cuenta propietaria o una cuenta administradora verificada.
- La tier list solo muestra campeones con una categoría competitiva válida. Los
  campeones marcados como `SIN DATOS`, incluido Sylas mientras no tenga métricas,
  permanecen disponibles en el catálogo y en las builds, pero no se presentan
  como una recomendación competitiva.
- Se guardó la medición de `motorola moto g56 5G` (2400×1080, API 36) en los
  valores predeterminados y en el respaldo de calibración. El perfil solo se
  aplica cuando coinciden fabricante, modelo y dimensiones exactas; una
  calibración guardada por el usuario mantiene prioridad.

## Versión y verificación

- Versión `1.1.10.512`, build `1228`.
- `testDebugUnitTest` ejecutó correctamente las pruebas de patrocinio,
  clasificación, tier list y perfiles de dispositivos, incluidas las nuevas
  pruebas de borrado/clasificación y Motorola.
- `validate-localized-resources.py`, `audit-portuguese.sh`,
  `update-champion-builds.py --check`, `git diff --check` y la validación JSON
  del respaldo de calibración terminaron correctamente.
