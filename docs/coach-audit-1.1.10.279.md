# Coach 1.1.10.279 — Tier List Personal y actualización del catálogo

- Versión anterior: 1.1.10.278, código 994.
- Versión nueva: 1.1.10.279, código 995.

## Problemas corregidos

- El resumen de enfrentamientos agrupaba únicamente por rival y podía mezclar partidas de campeones propios distintos. Ahora cada registro usa la pareja exacta campeón propio + rival, por lo que las victorias, derrotas y la tasa corresponden a un solo enfrentamiento.
- La tarjeta y el análisis usan «contra» para explicar la relación entre ambos campeones; se eliminó el `vs` ambiguo de esta vista.
- Al iniciar la aplicación se recarga el catálogo canónico incluido en la versión instalada aunque exista una caché anterior. Así las tasas de victoria, selección y bloqueo y las categorías de la Tier List no quedan congeladas por datos viejos de una instalación previa.
- La sincronización global también lee las tasas publicadas por la fuente que las ofrece, las promedia cuando hay más de una lectura, las guarda en la caché y las reaplica al abrir la aplicación. Las fuentes que solo publican Tier siguen sin inventar porcentajes.
- La ficha de campeón vuelve a calcular sus estadísticas y recomendaciones cuando cambia la revisión del catálogo.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/WildRiftApp.kt`
- `app/src/main/java/com/example/data/analytics/PersonalTierListManager.kt`
- `app/src/main/java/com/example/data/sync/RegionalTierParser.kt`
- `app/src/main/java/com/example/data/sync/GlobalTierSourceClient.kt`
- `app/src/main/java/com/example/data/sync/GlobalTierConsensus.kt`
- `app/src/main/java/com/example/data/sync/BestBuildWrScraper.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/ui/screens/PersonalTierListView.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/test/java/com/example/PersonalTierListRegressionTest.kt`
- `app/src/test/java/com/example/GlobalTierScrapingTest.kt`

## Verificación

- `git diff --check`: correcto.
- Se añadió una regresión que evita mezclar dos campeones propios contra el mismo rival.
- La prueba local no pudo descargar Gradle porque el entorno no permitió la conexión a `services.gradle.org`; la compilación y las pruebas completas deben ejecutarse en el flujo de validación.
