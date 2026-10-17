# Coach 1.1.10.308 — build 1024

## Problemas corregidos

- La clasificación y la ficha del campeón muestran ahora los mismos porcentajes de victoria, selección y bloqueo para la línea activa.
- El criterio se aplica al catálogo completo de 142 campeones y a cada línea secundaria con sus tres estadísticas publicadas.
- Una línea Flex incompleta deja de mostrarse hasta contar con victoria, selección y bloqueo propios.
- El control de actualización permite consultar y elegir una versión mínima entre las cinco publicaciones instalables más recientes.
- La versión obligatoria elegida se conserva al activar o desactivar la comprobación.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/assets/translations_pt.json`
- `app/src/main/java/com/example/ui/components/AdminDashboardDialog.kt`
- `app/src/main/java/com/example/ui/components/ChampionRateLine.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/main/java/com/example/ui/screens/TierListTab.kt`
- `app/src/main/java/com/example/util/AppUpdateManager.kt`
- `app/src/test/java/com/example/ChampionLaneStatsTest.kt`
- `app/src/test/java/com/example/LiveMetaRegressionTest.kt`
- `app/src/test/java/com/example/MetaRegionRegressionTest.kt`

## Verificación

- Prueba de coincidencia entre tarjeta y ficha para los 142 campeones y todas sus líneas publicadas.
- Prueba de exclusión de líneas Flex con estadísticas parciales.
- Prueba de filtrado y límite de las cinco versiones instalables más recientes.
- Auditoría estática de español y portugués.
- `git diff --check` sin errores.
- La compilación local queda delegada a Actions porque este entorno no incluye Android SDK.

## Versión

- `versionCode`: 1024
- `versionName`: 1.1.10.308
