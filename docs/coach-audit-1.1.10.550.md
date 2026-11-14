# Coach 1.1.10.550 — Cobertura real de estadísticas de WildRiftCore

Versión: 1.1.10.550; versionCode: 1266.

## Cambios

- Se mantiene la región visible como `China`, separada de Global, con una sola
  fuente Tencent y monitorización independiente.
- WildRiftCore ahora lee también el dataset JSON `rk-data` de su página de
  clasificaciones. La tabla HTML muestra solo una parte de las filas y por eso
  dejaba fuera estadísticas publicadas de campeones como Bard, Norra y Singed.
- La selección del rango respeta el rango publicado por la página y solo usa
  otros rangos para completar campos ausentes. No se sustituyen tasas actuales
  completas ni se fabrican porcentajes.
- Se conserva el filtro de tres tasas: un campeón sin victoria, selección y
  veto publicados no aparece como entrada competitiva.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/data/sync/RegionalTierParser.kt`
- `app/src/test/java/com/example/GlobalTierScrapingTest.kt`
- `docs/coach-audit-1.1.10.550.md`

## Verificaciones

- Se añadió una regresión para el dataset completo de clasificaciones de
  WildRiftCore, incluyendo estadísticas por línea.
- `./gradlew :app:compileDebugKotlin --offline`: pendiente de ejecutar tras el
  cambio de parser.
- Las pruebas unitarias requieren un JDK con `javac`; el entorno local solo
  expone el runtime de Java. La validación completa del APK queda para la
  ejecución de integración.

## Texto para pruebas

Coach 1.1.10.550

- Global sigue seleccionado por defecto y China aparece como región separada.
- El monitor conserva únicamente WildRiftFire y WildRiftCore para Global y la
  fuente Tencent para China.
- WildRiftCore recupera las estadísticas publicadas desde su dataset completo,
  aunque no estén renderizadas en la tabla inicial.
- Las tasas solo se muestran cuando están publicadas las tres métricas del
  campeón; los faltantes permanecen identificados y no se convierten en datos
  inventados.
