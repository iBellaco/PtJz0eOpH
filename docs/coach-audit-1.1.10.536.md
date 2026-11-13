# Auditoría Coach 1.1.10.536

- `versionCode`: `1252`.
- `versionName`: `1.1.10.536`.

## Corrección de estadísticas incompletas

- Se incorporó WildRifter como fuente estadística del parche actual. La página
  comprobada publica tres métricas válidas para 142 de 143 campeones; Sylas es
  el único registro sin datos publicados y queda fuera de la lista competitiva.
- La sincronización prioriza filas completas y solo combina campos faltantes
  antes de publicar. Una fila con win rate y pick rate pero sin ban rate no se
  guarda como estadística vigente.
- Las estadísticas globales y las estadísticas por línea exigen win rate,
  pick rate y ban rate. El detalle muestra las tres o indica que no hay datos;
  no mezcla valores actuales con un guion de una fila parcial.
- Las cachés anteriores también se filtran al iniciar para que un registro
  parcial no vuelva a aparecer antes de la siguiente sincronización.
- El monitoreo separa estadísticas completas, parciales y ausentes, e informa
  los identificadores afectados para distinguir una fuente sin conexión de una
  página que realmente no publica una métrica.

## Archivos principales modificados

- `BestBuildWrScraper`, `GlobalTierSourceClient`, `RegionalTierParser` y
  `GlobalTierConsensus` para la obtención, validación y fusión de métricas.
- `WildRiftRepository`, `TierListTab` y `MetaMonitoringCard` para evitar la
  representación de filas parciales y mostrar el diagnóstico de cobertura.
- Traducciones portuguesas, pruebas de scraping/visibilidad y validadores de
  contenido del parche.

## Verificación realizada

- Comprobación independiente de la página actual de WildRifter: 143 tarjetas,
  142 completas, 0 parciales y 1 sin datos (`sylas`); las métricas de cada fila
  quedaron dentro de 0–100.
- Pruebas unitarias añadidas para el parser WildRifter, las tres métricas
  obligatorias, el diagnóstico de filas parciales y la visibilidad de la tier
  list.
- `git diff --check` y validación JSON de traducciones sin errores.
- La validación de contenido de parche se actualizó para reconocer las tres
  fuentes activas y exigir `GamePatch.VERSION` en cada una.
- La compilación local no pudo ejecutarse porque la imagen disponible no tiene
  `javac`; la validación Android debe ejecutarse en CI con el SDK y JDK
  configurados.
