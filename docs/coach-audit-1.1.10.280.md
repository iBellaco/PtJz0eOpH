# Coach 1.1.10.280 — estadísticas dinámicas de Tier List

## Problema

Las tasas de victoria, selección y bloqueo podían quedarse con los valores del catálogo anterior. La actualización global solo se aplicaba cuando también se obtenían las categorías, y la pantalla podía conservar el cálculo anterior aunque el repositorio ya tuviera una revisión nueva.

## Cambios

- `GlobalTierSourceClient` considera válida una fuente si entrega categorías o tasas publicadas, y procesa las tasas aunque falte temporalmente el bloque de categorías.
- `GlobalTierConsensus` normaliza las claves y reemplaza las tasas nuevas sobre la instantánea guardada. Los valores antiguos solo se conservan para campeones que la fuente no publica en una tabla parcial.
- `BestBuildWrScraper` aplica una actualización de tasas aunque la categoría no esté disponible y calcula la variación de win, pick y ban contra la instantánea anterior.
- `WildRiftRepository` conserva la revisión observable y expone deltas calculados a partir de datos reales.
- La pantalla Tier List y el catálogo invalidan sus listas al cambiar la revisión del repositorio. Tier List incorpora el control de actualización manual.
- El arranque solicita la actualización global para evitar presentar una caché reciente como si fuera el parche actual.

## Pruebas

- Regresión de reemplazo de win, pick, ban y Tier en `MetaRegionRegressionTest`.
- Regresión de fusión de tasas con claves normalizadas en `GlobalTierScrapingTest`.
- `git diff --check` sin errores.
- La prueba Gradle local no pudo ejecutarse porque el entorno no tiene la distribución Gradle y no permite descargarla; la validación completa se ejecutará en GitHub Actions antes de fusionar.

