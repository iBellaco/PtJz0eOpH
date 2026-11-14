# Auditoría de Coach 1.1.10.544

## Alcance

- El monitor estadístico global queda limitado a los dos publicadores activos:
  WildRiftFire y WildRiftCore.
- Se eliminó la fuente estadística retirada de la sincronización, los parsers,
  las pruebas y la documentación histórica. También se invalidan las cachés
  de estadísticas cuando cambia el conjunto de publicadores, para que una tasa
  antigua no reaparezca como si proviniera del conjunto actual.
- La clasificación China / Local se incorporó como una vista independiente de
  la Tier List. Usa la clasificación T0–T5 incluida en el catálogo y la
  convierte a S+, S, A, B y C. No muestra WR, selección ni bloqueo porque la
  fuente local no ofrece las tres tasas actuales completas para todo el
  catálogo.
- Global sigue siendo la selección predeterminada. La preferencia elegida se
  conserva entre reinicios y se puede cambiar desde la vista Tier List. Sylas
  permanece fuera de la vista China / Local cuando no tiene clasificación.
- El catálogo identifica la procedencia activa en español y portugués.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/data/sync/BestBuildWrScraper.kt`
- `app/src/main/java/com/example/data/sync/GlobalMetaSyncService.kt`
- `app/src/main/java/com/example/data/sync/MetaRegion.kt`
- `app/src/main/java/com/example/data/sync/PublishedStatsParser.kt`
- `app/src/main/java/com/example/data/sync/RegionalTierParser.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/ui/screens/TierListTab.kt`
- `app/src/main/java/com/example/ui/screens/ChampionsCatalogTab.kt`
- `app/src/main/java/com/example/ui/components/MetaMonitoringCard.kt`
- Traducciones, pruebas de sincronización y regresiones de región.

## Verificaciones

- `git diff --check`: correcto.
- Los JSON de traducciones español y portugués se analizaron correctamente.
- La búsqueda del identificador de la fuente retirada no encuentra referencias
  en el código ni en la documentación versionada.
- La prueba Android local no pudo iniciarse en este entorno porque no existe
  la ruta del Android SDK (`local.properties`/`ANDROID_HOME`). Debe ejecutarse
  la validación Android en el entorno que tenga SDK y JDK configurados.

## Fuente externa pendiente

`WildRiftStats.org` respondió HTTP 523 en la comprobación de esta revisión,
incluidos `robots.txt` y `sitemap.xml`; por ello no se afirma una cobertura de
campeones ni se importan datos de ese sitio. El catálogo de Coach contiene 143
campeones, pero esa cifra no representa la cobertura estadística del sitio.
