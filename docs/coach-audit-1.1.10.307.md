# Coach 1.1.10.307 — build 1023

## Problemas corregidos

- Los 142 campeones conservan estadísticas visibles usando su base local validada cuando una consulta automática llega incompleta.
- Las etiquetas Flex del catálogo solo muestran líneas con estadísticas publicadas específicas para esa combinación campeón/línea.
- Los filtros y contadores por línea usan el mismo criterio de líneas publicadas.
- La variación sin historial de 24 horas muestra un estado neutro en lugar de `+0.00%`.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/util/RegionalStatsFormat.kt`
- `app/src/main/java/com/example/ui/screens/ChampionsCatalogTab.kt`
- `app/src/test/java/com/example/MetaRegionRegressionTest.kt`

## Verificación

- Cobertura automatizada del roster completo de 142 campeones y sus tres porcentajes.
- Uso compartido del criterio de líneas publicadas entre catálogo, filtros y ficha del campeón.
- `git diff --check` sin errores.

## Versión

- `versionCode`: 1023
- `versionName`: 1.1.10.307
