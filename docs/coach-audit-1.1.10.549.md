# Coach 1.1.10.549 — Scraping regional y monitoreo por fuente

Versión: 1.1.10.549; versionCode: 1265.

## Cambios

- La etiqueta de la región pasa a ser únicamente `China` en español y
  portugués. Global continúa siendo la vista seleccionada por defecto.
- Se incorporó el feed oficial de Tencent para China. La página de
  `lolm.qq.com` se consume mediante sus dos respuestas JSON públicas: catálogo
  de héroes y ranking por posición. La asociación de identificadores Tencent a
  campeones de Coach es explícita y no depende de una traducción aproximada.
- El monitoreo muestra la fuente de la región activa: WildRiftFire y
  WildRiftCore para Global, o la única fuente Tencent para China.
- La vista China se actualiza con las tasas publicadas de victoria, selección y
  veto. Los campeones sin las tres tasas actuales no se muestran como entradas
  competitivas y se enumeran como cobertura faltante en el detalle de la
  fuente.
- WildRiftFire ahora tolera el esquema actual de varios rangos y completa una
  fila ausente del rango principal con el rango más completo sin sobrescribir
  los valores principales.
- La lectura de estadísticas conserva respuestas válidas cuando una consulta
  histórica o un bloque secundario no está disponible; ya no convierte la
  ausencia del historial en un fallo de toda la fuente.
- Se mantiene la separación de estadísticas parciales: nunca se inventa una
  tasa de veto para convertir una fila incompleta en una fila competitiva.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/data/sync/ChinaLolmParser.kt`
- `app/src/main/java/com/example/data/sync/GlobalTierSourceClient.kt`
- `app/src/main/java/com/example/data/sync/PublishedStatsParser.kt`
- `app/src/main/java/com/example/data/sync/BestBuildWrScraper.kt`
- `app/src/main/java/com/example/data/sync/MetaRegion.kt`
- `app/src/main/java/com/example/data/sync/GlobalMetaSyncService.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/ui/components/MetaMonitoringCard.kt`
- `app/src/main/java/com/example/ui/screens/TierListTab.kt`
- Recursos de texto en español y portugués.

## Verificaciones

- `./gradlew :app:compileDebugKotlin --offline`: correcto.
- Pruebas unitarias nuevas para la conversión del feed Tencent, las posiciones
  y el llenado seguro de filas de WildRiftFire.
- `git diff --check`: correcto.

La ejecución de pruebas unitarias requiere un JDK con `javac`; el entorno local
solo expone el runtime de Java. La validación completa del APK y la consulta en
dispositivo deben ejecutarse en el entorno de integración.

## Texto para pruebas

Coach 1.1.10.549

- La clasificación ahora muestra Global por defecto y permite elegir China.
- China obtiene sus datos del ranking oficial de Tencent y aparece en el
  monitoreo de la región activa.
- Las tasas se muestran solo cuando victoria, selección y veto están publicados
  para el campeón; los casos incompletos quedan identificados en el detalle de
  la fuente.
- Corregida la lectura de WildRiftFire cuando el sitio publica varios rangos.
- Corregida la conservación de datos válidos cuando falla un bloque secundario
  o una consulta histórica.
