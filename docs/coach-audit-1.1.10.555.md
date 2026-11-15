# Coach 1.1.10.555 — roles rivales, evidencia de diagnóstico y estadísticas globales

Versión: 1.1.10.555; versionCode: 1271.

## Cambios

- La asignación de líneas rivales deja de usar la posición de la fila. Combina
  rol primario, roles flexibles, estadísticas por línea disponibles y cobertura
  de la composición. La solución protege los roles únicos y evita inventar un
  ADC secundario cuando existe una línea de Barón válida; la combinación
  Lux/Yasuo/Fizz queda como Soporte/Mid/Top.
- El botón de diagnóstico adjunta una captura completa de MediaProjection o,
  si no hay un frame literal disponible, el último frame procesado. La imagen
  conserva los avatares detectados y dibuja las cajas, líneas y círculos de la
  geometría usada, sin activar ni mover el visor del remitente. El texto indica
  origen, tamaño, antigüedad, hash y cantidad de identidades representadas.
- Las tarjetas de recomendación usan una frase corta y específica por campeón,
  rival, línea y habilidad. El análisis ampliado conserva el detalle completo.
- Se estabilizó el scroll de la vista de draft y del overlay con contenedores
  medibles y etiquetas de prueba.
- WildRiftFire conserva filas completas aunque el publicador omita la marca
  `main`; el cliente amplió sus tiempos de lectura y reintenta fallos de red.
  WildRiftFire y WildRiftCore siguen siendo las dos fuentes globales activas.

## Verificaciones de fuentes

- Consulta HTTP del 15/11/2026: ambas páginas de tier respondieron 200 y
  declararon el parche 7.3f.
- El parser de la página actual produjo 143 categorías en cada fuente.
- La página de estadísticas actual produjo 141 registros completos en
  WildRiftFire y 141 en WildRiftCore, incluyendo estadísticas por línea. Los
  dos campeones sin registro de tasas en ambos rankings se mantienen como
  ausencia publicada; no se generan números inventados.
- Se añadió una regresión para conservar una fila completa sin `main` y se
  mantuvieron las pruebas de los esquemas actuales de ambas fuentes.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/service/screen/EnemyRoleInference.kt`
- `app/src/main/java/com/example/util/ChampionMatchupCoaching.kt`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/src/main/java/com/example/ui/components/DeviceDiagnosticButton.kt`
- `app/src/main/java/com/example/ui/components/CalibrationControls.kt`
- `app/src/main/java/com/example/service/screen/CalibrationEvidenceImage.kt`
- `app/src/main/java/com/example/data/sync/PublishedStatsParser.kt`
- `app/src/main/java/com/example/data/sync/GlobalTierSourceClient.kt`
- `app/src/main/java/com/example/service/FloatingDraftCoachView.kt`
- `app/src/main/java/com/example/ui/screens/DraftAnalysisTab.kt`

## Pruebas dirigidas

- `EnemyRoleInferenceTest`
- `CoachingGeneratorLocalizationTest`
- `DraftRecommendationQualityTest`
- `GlobalTierScrapingTest`
- `MatchupReferenceSourcesTest`
- `CalibrationReportTest`
- `DraftMultideviceBoxesTest`

## Texto para pruebas

Coach 1.1.10.555

- Las líneas rivales se estiman por composición y roles flexibles, no por el
  orden visual de selección.
- El informe de diagnóstico incluye una imagen completa con los avatares y la
  geometría usada para revisar la alineación.
- Las recomendaciones muestran acciones breves y diferentes para cada
  campeón.
- Las vistas de draft y del asistente permiten desplazar todo el contenido.
- Las estadísticas globales continúan tomando datos de las dos fuentes
  publicadas y no rellenan valores ausentes con cifras inventadas.
