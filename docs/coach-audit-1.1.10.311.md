# Coach 1.1.10.311 — build 1027

## Problemas corregidos

- Una cuenta heredada cuyo rol guardado todavía era `free` ya no muestra «Gratis · ACTIVO» cuando su suscripción Premium está vigente; el gestor presenta Premium como su rol efectivo.
- La actividad de usuarios incorpora la categoría Premium con totales y conectados en tiempo real. Las cuentas gratuitas con una suscripción vigente ya no se cuentan también como gratuitas.
- La pantalla de gestión mantiene activo el ciclo de consulta de fuentes cada minuto. Una advertencia real de contenido sin cambios, incluida WildRiftGuides, permanece visible y desaparece automáticamente cuando la fuente publica una modificación.
- «Campeones actualizados» mide la cobertura combinada del catálogo: categorías publicadas o estadísticas publicadas. Esto permite representar correctamente los 142 campeones sin inventar tasas para los seis que las fuentes de porcentajes no publican actualmente.
- La fusión de estadísticas reconoce WildRiftGuides con su identificador correcto.
- La versión publicada se restaura desde la comprobación válida guardada y también aparece de forma informativa junto al control de verificación. Al abrir la gestión se fuerza una comprobación nueva.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/data/sync/BestBuildWrScraper.kt`
- `app/src/main/java/com/example/data/sync/MetaFreshness.kt`
- `app/src/main/java/com/example/model/PremiumAccessPolicy.kt`
- `app/src/main/java/com/example/ui/components/AdminDashboardDialog.kt`
- `app/src/main/java/com/example/ui/components/AdminUserPresenceCard.kt`
- `app/src/main/java/com/example/ui/components/MetaMonitoringCard.kt`
- `app/src/main/java/com/example/ui/components/UserDetailManagementDialog.kt`
- `app/src/main/java/com/example/util/AppUpdateManager.kt`
- `app/src/test/java/com/example/LiveMetaRegressionTest.kt`
- `app/src/test/java/com/example/RuntimeBehaviorTest.kt`
- `docs/CONTINUIDAD_COORDENADAS_MULTIDISPOSITIVO.md`

## Verificación

- Catálogo generado validado: seis secciones sin divergencias.
- Iconos locales validados: 52 archivos WebP compartidos.
- Builds verificadas: 142 campeones y 300 configuraciones por línea.
- Auditoría portuguesa: 2626 textos visibles, sin residuos en español.
- Se añadieron regresiones para el rol efectivo Premium, vencimiento/bloqueo y cobertura combinada 142/142.
- Se documentaron los perfiles y espacios de coordenadas de Red Magic 10 Pro, Poco X7 Pro y S26 Pro para continuidad entre conversaciones.
- `git diff --check` sin errores.
- El entorno local no incluye Android SDK; la compilación R8, las pruebas Android y la instalación se ejecutan en los controles obligatorios de Actions.

## Versión

- `versionCode`: 1027
- `versionName`: 1.1.10.311
