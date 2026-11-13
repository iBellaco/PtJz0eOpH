# Auditoría Coach 1.1.10.531

- `versionCode`: `1247`.
- `versionName`: `1.1.10.531`.

## Corrección de estadísticas publicadas

- Se reconoce la estructura actual `rkc-*` de la tabla de estadísticas de WildRiftCore sin perder compatibilidad con la estructura anterior.
- Se recuperan por separado las tasas de victoria, selección y bloqueo, incluida la línea publicada en cada fila.
- La estadística global se toma exclusivamente del panel `ALL`; las filas por línea quedan como respaldo y no la sobrescriben.
- Cuando solo existe un fragmento por líneas, el respaldo usa de forma determinista la fila con mayor tasa de selección en lugar de depender del último elemento HTML.
- Un porcentaje retirado por las fuentes deja de marcarse como recién consultado: la caché solo se conserva cuando falla por completo la actualización.
- Los valores locales de catálogo ya no se presentan como estadísticas publicadas si no existe una observación vigente.
- El monitor distingue una fuente parcial de un error de consulta y muestra la cobertura combinada publicada, no solo el número de categorías.
- El monitor separa categorías de estadísticas actuales para no presentar `143/143` cuando solo existen tasas para 136 campeones.
- Se amplía de forma acotada el margen de respuesta para el archivo histórico, que ya se acerca al límite anterior.
- Se mantiene la información equivalente en español y portugués.

## Archivos modificados

- `app/src/main/java/com/example/data/sync/RegionalTierParser.kt`
- `app/src/main/java/com/example/ui/components/MetaMonitoringCard.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/data/sync/BestBuildWrScraper.kt`
- `app/src/main/java/com/example/data/sync/GlobalTierSourceClient.kt`
- `app/src/main/java/com/example/util/RegionalStatsFormat.kt`
- `app/src/main/assets/translations_ui_pt.json`
- `app/src/test/java/com/example/GlobalTierScrapingTest.kt`
- `app/src/test/java/com/example/GlobalTierSourceClientTest.kt`
- `app/build.gradle.kts`

## Verificaciones realizadas

- Pasaron `GlobalTierScrapingTest`, `GlobalTierSourceClientTest` y `MetaRegionRegressionTest`.
- Se comprobaron los esquemas anterior y actual de WildRiftCore.
- Se verificaron las filas repetidas por línea y que `ALL` no sea reemplazado por TOP, MID, JUNGLE, ADC o SUPPORT.
- Se verificó que una tasa retirada no renueve su fecha desde la caché.
- El JSON de traducción es válido.
- Las páginas públicas 7.3f respondieron correctamente: Core publicó 143 categorías, 136 estadísticas globales y 180 filas por rol; Fire publicó 142 categorías y 136 estadísticas globales.
