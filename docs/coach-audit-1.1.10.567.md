# Coach 1.1.10.567 — meta China pública, builds por línea y perfil reactivo

## Correcciones implementadas

- La instantánea publicada de estadísticas de China se escucha sin exigir una cuenta y las reglas permiten su lectura pública; la escritura continúa restringida al proceso autorizado. Los datos de tasas, segmentos de ELO y movimientos de tier se mantienen en una misma instantánea para todos los dispositivos.
- La sincronización de China espera una lectura inicial breve de la instantánea compartida antes de consultar el feed de respaldo, reduciendo divergencias durante el primer arranque.
- El marco del avatar del usuario se actualiza inmediatamente cuando el listener del perfil recibe un cambio de rol secundario.
- La ficha del campeón incorpora selectores compactos y legibles para Global/China y, en China, para el segmento de ELO. Cada control conserva un área táctil mínima de 48 dp y la ficha recalcula sus estadísticas y build al cambiar de servidor.
- Las builds se separan por línea sin reutilizar automáticamente la build de otro rol. Jungla siempre muestra Castigo; Soporte conserva o inserta exactamente una misión de soporte; los objetos core y situacionales se normalizan sin repeticiones.
- La sección 4 de Información centra título y contenido, y el texto deja de describirse como una comunidad «competitiva».
- La ficha de Información ya no muestra número de compilación ni la palabra «compilación»; la identidad completa con versión y build permanece únicamente en la pantalla principal.

## Versión

- `versionCode`: 1283
- `versionName`: `1.1.10.567`

## Archivos principales

- `app/src/main/java/com/example/data/sync/ChinaMetaSnapshotStore.kt`
- `app/src/main/java/com/example/data/sync/BestBuildWrScraper.kt`
- `firestore.rules`
- `app/src/main/java/com/example/util/SubscriptionManager.kt`
- `app/src/main/java/com/example/util/ChampionRoleAdapter.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/ui/screens/InfoScreen.kt`
- `app/src/main/res/values/startup_information.xml`
- `app/src/main/res/values-pt/startup_information.xml`
- `app/src/main/assets/translations_ui_pt.json`
- `app/src/test/java/com/example/ChampionRoleAdapterBuildRulesTest.kt`

## Verificaciones

- `git diff --check`: completada.
- XML de recursos español y portugués: parseado correctamente.
- YAML de los seis workflows: parseado correctamente con PyYAML.
- `bash tools/audit-portuguese.sh`: completada sin hallazgos de español visible.
- Búsqueda de «Compilación» y «Compilação» en la interfaz: sin coincidencias visibles.
- `./gradlew :app:testDebugUnitTest --offline`: no inició porque el wrapper necesita descargar Gradle 9.3.1 y la conexión de este entorno fue rechazada. La validación ejecutable queda pendiente en CI.
