# Coach 1.1.10.275 (build 991)

## Cambios

- Historial de drafts: los administradores tienen un botón de prueba que agrega 20 partidas de clasificatoria normal y 20 de clasificatoria legendaria al perfil activo para validar historial y Tier List Personal.
- Clasificación y detalle de campeón: la gráfica de tendencia vuelve a mostrar los tres puntos solicitados: hace 24 horas, hace 12 horas y actual.
- Builds de campeón: las runas situacionales usan el mismo tamaño visual que las demás runas secundarias. La ventana de información de runas limita su altura y permite scroll cuando el contenido no entra en pantalla.
- Catálogo de builds: se regeneraron las 300 builds oficiales con alternativas completas del catálogo para curación, escudos, tanques y resistencias. Tridente de Oceánida queda disponible como respuesta AP contra escudos.
- Reglas de objetos: se mantiene que soporte usa exactamente Hoz espectral o Escudo reliquia como objeto básico obligatorio, y ningún otro rol usa componentes básicos o de nivel medio en las builds.
- Análisis táctico: se actualizaron los consejos generados junto con los objetos para conservar formato Soberano y decisiones por campeón, línea y compra.

## Archivos

`app/build.gradle.kts`, `app/src/main/java/com/example/data/repository/DraftHistoryRepository.kt`, `app/src/main/java/com/example/ui/screens/DraftHistoryScreen.kt`, `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`, `app/src/main/java/com/example/ui/components/SparklineTrendGraph.kt`, `app/src/main/java/com/example/ui/screens/ChampionBuildDetails.kt`, `app/src/main/assets/champions_creator_builds.json`, `app/src/main/assets/translations_pt.json`, `app/src/main/res/raw/champions_part1.json`, `app/src/main/res/raw/champions_part2.json`, `tools/build_coaching.py`, `tools/update-champion-builds.py`, `app/src/test/java/com/example/ChampionBuildsCatalogValidationTest.kt`.

## Validación

- Pendiente de ejecución local/CI: catálogo de builds, reglas de objetos, pruebas de historial, renderizado y suite Android.
