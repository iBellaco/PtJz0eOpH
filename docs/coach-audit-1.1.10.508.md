# Coach 1.1.10.508 — Corrección de traducción portuguesa y respaldo de coordenadas

## Corrección

- Se reemplazó el mensaje de error literal de eliminación de anuncios por un
  recurso localizado; el portugués ya no recibe una frase en español en esa ruta.
- Se incorporó un respaldo versionado de cinco mediciones normalizadas de
  coordenadas aportadas por el propietario para comparar distintas dimensiones
  de captura.
- El respaldo se conserva como evidencia y no activa presets automáticos por
  fabricante o modelo; la adaptación continúa dependiendo del viewport observado.

## Versión y verificación

- `1.1.10.508`, build `1224`.
- La ejecución roja `fix-error` 42938001207 identificó exactamente el literal
  `No se pudo eliminar el anuncio` en `SponsorCpmPanelDialog.kt` durante la
  auditoría portuguesa.
- Se validaron los recursos español/portugués, la sintaxis JSON y la compilación
  de los recursos modificados localmente.
