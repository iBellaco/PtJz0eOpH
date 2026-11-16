# Coach 1.1.10.556 — monitoreo por servidor, China por Elo y marcos sincronizados

Versión: 1.1.10.556; versionCode: 1272.

## Cambios

- El monitoreo administrativo muestra y actualiza todas las fuentes activas,
  aunque la tier list tenga seleccionado otro servidor. Cada fila identifica
  explícitamente si pertenece a Global o China; la fila China identifica el
  origen Tencent y sus cuatro segmentos Elo publicados.
- El parser de Tencent conserva por separado los segmentos Diamante+, Maestro+,
  Retador y Grieta del Invocador. La tier list China ofrece un selector propio
  de segmento Elo, separado visualmente del selector de servidor, y guarda la
  elección para la siguiente sesión.
- El botón de información de la pantalla principal tiene un pulso visual sutil
  que mantiene su accesibilidad y no altera su acción.
- Al cambiar el rol secundario de una cuenta, el marco asociado se reemplaza en
  la misma escritura si estaba usando el marco del rol anterior. Los marcos
  elegidos manualmente que no pertenecen al rol secundario se conservan.

## Comprobaciones de datos

- La página de China respondió y su payload actual publicó las claves de
  segmento 0, 1, 2, 3 y 4; los cuatro segmentos visibles contienen filas.
- El parser aplicado al payload actual conservó los cuatro grupos Elo.
- No se mezclan los datos China con Global ni se generan estadísticas ausentes.

## Pruebas dirigidas

- `ChinaLolmParserTest`
- `MetaRegionRegressionTest`
- `SecondaryRoleFrameTest`
- `GlobalTierScrapingTest`
- `EnemyRoleInferenceTest`
- `CoachingGeneratorLocalizationTest`
- `DraftRecommendationQualityTest`
- `MatchupReferenceSourcesTest`
- `CalibrationReportTest`
- `DraftMultideviceBoxesTest`

La compilación `compileDebugKotlin` también terminó correctamente. La
comprobación de la fuente China se hizo con el payload HTTP guardado de la
consulta actual y con el parser; así se verificó que no se pierde ningún
segmento al separar por Elo.

## Texto para pruebas

Coach 1.1.10.556

- El monitoreo administrativo mantiene visibles Global y China al cambiar la
  selección de la tier list.
- China permite cambiar entre cuatro segmentos Elo publicados y muestra cuál
  está activo.
- El botón de información tiene una animación discreta y sigue abriendo la
  pantalla informativa.
- Al cambiar el rol secundario, el marco de ese rol se actualiza sin borrar un
  marco independiente elegido manualmente.
