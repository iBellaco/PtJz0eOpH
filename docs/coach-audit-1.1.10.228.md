# Coach 1.1.10.228 — código 944

## Cambios

- `firestore.rules`, `storage.rules`: los permisos del personal comprueban suspensión y perfil activo. Los vídeos se limitan a 20 MiB y a titulares habilitados.
- `server/economy/queue.mjs`, `worker.mjs`, `firestore.indexes.json`, `firebase.json`, workflows y pruebas: orden por antigüedad, recuperación separada de solicitudes nuevas, reintentos acotados y revisión manual con el mismo identificador. El flujo de permisos publica los índices antes de activar el procesador; existe una consulta de respaldo mientras se construyen.
- `EconomyServiceClient.kt`, `EconomyPendingStatus.kt`, traducciones y pruebas de pantalla: se informa cuándo el servicio está retrasado o una operación necesita revisión, sin permitir otra operación mientras esa cuenta esté pendiente.
- `RolePanelAccess.kt`, `PremiumAccessPolicy.kt`, `PremiumGrantPolicy.kt`, diálogo de usuarios y pruebas: los controles administrativos dependen del permiso firmado; los roles conservan su tratamiento Premium sin conceder autoridad.
- `NoticeMediaStorageManager.kt` y pruebas de medios: se rechazan vídeos que superen el límite también antes de subirlos.
- `.github/workflows/build-apk.yml`: lint y todas las pruebas unitarias Android pasan antes de compilar el APK final; continúan las comprobaciones de pantallas, firma e instalación.
- `gradle/libs.versions.toml`, `app/build.gradle.kts`: las APIs de fecha utilizadas por Coach se adaptan a Android 7.0 y posteriores; lint había detectado una incompatibilidad en API 24/25.
- `.github/scripts/cleanup-completed-runs.cjs`, su prueba y `AGENTS.md`: la evidencia de las entregas se conserva separada del historial de ejecuciones frecuentes del servicio.
- `README.md`, `docs/economy-operations.md`, `app/build.gradle.kts`: documentación de operación y versión actualizadas.

## Verificación

Las pruebas aisladas de permisos, almacenamiento, eliminación de cuentas y economía pasan con emuladores. Las 43 pruebas unitarias Node de economía, la retención de Actions, el formato JSON/YAML y `git diff --check` pasan. El entorno local carece de Android SDK y de la configuración privada del cliente: lint, pruebas Android, R8, instalación y firma quedan como gates obligatorios de Actions antes de la publicación final.

## Resumen para testers

Coach 1.1.10.228: comprobar que las cuentas suspendidas no acceden a funciones del equipo, que los vídeos de más de 20 MiB se rechazan, que las solicitudes muestran retraso o revisión cuando corresponde y que un reintento no duplica el cargo. Revisar estas pantallas en español y portugués, confirmar la versión visible y verificar la actualización con el APK firmado.
