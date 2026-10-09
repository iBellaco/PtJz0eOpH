# Coach 1.1.10.233 — código 949

## Cambios

- `StreamerRepository.kt`: la comprobación de Streamer se mantiene al enviar un canal. La revisión solo comprueba la autorización de la cuenta administradora, para que pueda aprobar solicitudes válidas aunque el perfil del solicitante haya cambiado después de enviarla.
- `StreamerPanels.kt` y recursos en español y portugués: aprobar o rechazar un canal pide confirmación antes de guardar el cambio.
- `service.mjs`, `UserManagementActions.kt` y `UserDetailManagementDialog.kt`: retirar Premium actualiza de inmediato el perfil mostrado. Un perfil con rol Premium o Moderador pasa a Gratis; los demás roles conservan su rol y pierden únicamente el tiempo Premium. El rol Administrador sigue protegido.
- `UserDetailManagementDialog.kt` y `translations_ui_pt.json`: las duraciones rápidas de Premium y su retirada solicitan confirmación antes de aplicarse, también en portugués.
- `emulator.integration.mjs`: se cubren los tres resultados de retirada de Premium y la protección del rol Administrador.
- `app/build.gradle.kts`: versión 1.1.10.233, código 949.

## Verificación

- `npm test` en `server/economy`: 44 pruebas correctas.
- `npm test` en `tests/firestore`: 56 escenarios de reglas, recuperación de cuenta y economía correctos en emuladores.
- `python3 tools/update-champion-builds.py --check`, `bash tools/audit-portuguese.sh`, validación JSON y `git diff --check`: correctos.
- La prueba local de Gradle no pudo iniciarse porque el entorno bloquea la descarga de Gradle 9.3.1. La compilación release, firma, ofuscación e instalación se ejecutarán en las comprobaciones obligatorias antes de publicar.

## Resumen para testers

Coach 1.1.10.233: enviar una solicitud de canal desde una cuenta Streamer y aprobarla desde una cuenta administradora. Comprobar que aprobar y rechazar piden confirmación. Verificar que quitar Premium cambia Premium y Moderador a Gratis, mantiene los demás roles y no permite quitar el acceso vitalicio de Administrador. Confirmar que las duraciones rápidas de Premium piden confirmación.
