# Coach 1.1.10.566 — temas regionales legibles y meta China sincronizada

## Corrección implementada

- Se añadieron los tres indicadores de color y el candado visual a las tarjetas de temas regionales, manteniendo los doce temas independientes de la aplicación como catálogo Premium separado.
- Se redujo la saturación de las paletas regionales salvo Piltover, conservando contraste suficiente para títulos, etiquetas y estados.
- Los selectores Global, China y los cuatro segmentos de ELO de China ahora animan escala, color y borde según el tema de aplicación activo.
- El selector de China dejó de usar una paleta fija y toma la superficie y los acentos del tema seleccionado.
- La meta de China conserva el último snapshot local y puede distribuir el mismo snapshot, estadísticas por segmento ELO y movimientos de tier entre dispositivos autenticados; los dispositivos de usuario priorizan ese snapshot compartido para no divergir en el siguiente ciclo; los cambios ascendentes y descendentes ya no se fuerzan a cero.
- La ficha de cada campeón identifica el servidor consultado y, para China, el segmento ELO activo.
- Se ampliaron las traducciones portuguesas de los nuevos estados de servidor y segmentos de ELO, y se revisó la auditoría de texto visible.
- Se conserva la animación de descubrimiento del botón de información de la pantalla principal.

## Versión

- `versionCode`: 1282
- `versionName`: `1.1.10.566`

## Archivos principales

- `app/src/main/java/com/example/ui/theme/AppThemeManager.kt`
- `app/src/main/java/com/example/ui/components/ThemeCustomizationDialog.kt`
- `app/src/main/java/com/example/ui/screens/TierListTab.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/data/sync/BestBuildWrScraper.kt`
- `app/src/main/java/com/example/data/sync/ChinaMetaSnapshotStore.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/assets/translations_ui_pt.json`
- `app/src/test/java/com/example/MetaRegionRegressionTest.kt`

## Verificaciones

- `git diff --check`
- Validación JSON de `translations_ui_pt.json` con `jq`
- `bash tools/audit-portuguese.sh`: completada sin hallazgos de español visible
- Se añadió una prueba de regresión para conservar el movimiento de tier de China
- `./gradlew :app:compileDebugKotlin --offline`: pendiente en este entorno; el wrapper intentó descargar Gradle 9.3.1 y la conexión fue rechazada antes de iniciar la compilación
