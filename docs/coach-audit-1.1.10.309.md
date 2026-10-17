# Coach 1.1.10.309 — build 1025

## Problemas corregidos

- Al activar `1er Pick`, las recomendaciones de la línea elegida aparecen incluso cuando todavía no existe ningún campeón seleccionado.
- La verificación de actualización conserva únicamente el interruptor de activación; la aplicación comprueba automáticamente la publicación instalable más reciente y ya no permite elegir manualmente una versión obligatoria.
- Al confirmar diez campeones únicos mediante el escáner, el draft se guarda automáticamente con resultado pendiente sin abrir el formulario de guardado.
- Los informes tardíos del clasificador ya no pueden sustituir al décimo campeón después de cerrar y guardar el draft.
- `Vaciar` y la desactivación del overlay borran selecciones, baneos, memoria temporal, máscaras de pantalla, métricas del escáner y recortes diagnósticos para que no pasen datos a la siguiente sesión.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/assets/translations_pt.json`
- `app/src/main/java/com/example/service/FloatingAssistantService.kt`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/main/java/com/example/service/ConfirmedLastPickHud.kt`
- `app/src/main/java/com/example/service/OverlayState.kt`
- `app/src/main/java/com/example/service/screen/DraftVisionScanner.kt`
- `app/src/main/java/com/example/ui/components/AdminDashboardDialog.kt`
- `app/src/main/java/com/example/ui/screens/DraftAnalysisTab.kt`
- `app/src/main/java/com/example/util/AppUpdateManager.kt`
- `app/src/test/java/com/example/DraftAutoStopTest.kt`
- `app/src/test/java/com/example/DraftRecommendationQualityTest.kt`
- `app/src/test/java/com/example/DraftSessionLifecycleTest.kt`
- `app/src/test/java/com/example/LiveMetaRegressionTest.kt`

## Verificación

- Regresión de recomendaciones con primer pick y draft vacío.
- Regresión de cierre del escáner al confirmar el décimo campeón sin abrir el formulario manual.
- Regresión de limpieza de selecciones, archivos, estadísticas, máscaras y memoria del escáner.
- Auditoría de traducción portuguesa sin residuos en español.
- Catálogo regenerado y validado: 142 campeones y 300 builds por línea.
- `git diff --check` sin errores.
- El entorno local no incluye Android SDK; la compilación R8, las pruebas completas y la instalación quedan a cargo de los controles obligatorios de Actions.

## Versión

- `versionCode`: 1025
- `versionName`: 1.1.10.309
