# Coach 1.1.10.310 — build 1026

## Problemas corregidos

- El gestor de usuarios conecta su listado únicamente cuando la sesión administrativa o de moderación ya está disponible y vuelve a suscribirse automáticamente si la lectura inicial falla durante el inicio de sesión.
- Las cuentas creadas desde otro dispositivo aparecen mediante la escucha en tiempo real sin exigir cerrar y volver a iniciar sesión.
- Al conceder Premium manualmente, una cuenta con rol `free` cambia a `premium` para que su tarjeta deje de mostrarse como gratuita.
- La concesión Premium conserva los roles de creador, streamer, patrocinador, moderador y cualquier rol secundario existente.
- Al retirar Premium, únicamente el rol principal `premium` vuelve a `free`; los demás roles y el rol secundario se conservan.
- Las reglas de acceso aceptan únicamente esas transiciones y mantienen protegidos los campos de rol y suscripción.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/data/AdminPremiumRepository.kt`
- `app/src/main/java/com/example/data/PremiumGrantPolicy.kt`
- `app/src/main/java/com/example/ui/components/UserManagementPanel.kt`
- `app/src/test/java/com/example/InboxSynchronizationTest.kt`
- `app/src/test/java/com/example/RuntimeBehaviorTest.kt`
- `app/src/test/java/com/example/RuntimeVisibilityTest.kt`
- `firestore.rules`
- `tests/firestore/rules.test.mjs`

## Verificación

- 60 escenarios de reglas ejecutados correctamente en emuladores locales.
- Cobertura de promoción `free` a `premium` y conservación de roles ocupados.
- Cobertura de retirada Premium sin degradar moderadores ni eliminar roles secundarios.
- Auditoría portuguesa: 2625 textos visibles, sin residuos en español.
- Catálogo validado: 142 campeones y 300 builds por línea.
- `git diff --check` sin errores.
- El entorno local no incluye Android SDK; la compilación R8, las pruebas Android completas y la instalación quedan a cargo de los controles obligatorios de Actions.

## Versión

- `versionCode`: 1026
- `versionName`: 1.1.10.310
