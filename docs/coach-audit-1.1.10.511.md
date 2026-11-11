# Coach 1.1.10.511 — Corrección de validación del catálogo 7.3f

## Correcciones

- Se añadió la traducción portuguesa de la descripción actualizada de Aplastar
  para evitar que el texto español aparezca al cambiar el idioma.
- Se actualizó el caso de presentación de Eco discordante para comprobar los
  valores oficiales de 7.3f: 80 + 9% de poder de habilidad y 22,5 + 1,4%
  por proyectil secundario no utilizado.

## Versión y verificación

- Versión `1.1.10.511`, build `1227`.
- `python3 tools/update-champion-builds.py --check` confirmó que los datos
  generados están actualizados (143 campeones y 302 builds).
- `validate-localized-resources.py` y `audit-portuguese.sh` terminaron sin
  hallazgos; la auditoría portuguesa cubrió 302 perfiles y 5442 textos
  distintos generados.
- Las pruebas `PortugueseGeneratedSurfaceTest`,
  `RequiredItemCorrectionsTest`, `Patch73fCatalogTest`,
  `ChampionBuildsCatalogValidationTest` y `BuildCoachingRegressionTest`
  terminaron correctamente.
