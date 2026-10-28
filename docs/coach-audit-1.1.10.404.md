# Coach 1.1.10.404

## Cambio

- Las solicitudes de anuncios patrocinados ahora se muestran en el mismo flujo de solicitudes de verificación, roles y streamers.
- El contador administrativo incluye las solicitudes de patrocinio pendientes.
- Se eliminó el panel independiente de moderación de patrocinadores y su contador separado.
- Aprobar o rechazar un patrocinio desde el panel unificado actualiza su publicación.

## Archivos principales

- `app/src/main/java/com/example/ui/components/ModeratorRequestsDialog.kt`
- `app/src/main/java/com/example/data/PanelNotificationStore.kt`
- `app/src/main/java/com/example/data/PanelNotificationState.kt`
- `app/src/main/java/com/example/ui/auth/AuthScreen.kt`
- `app/src/main/java/com/example/ui/components/AdminDashboardDialog.kt`

## Verificación

- Se actualizaron las pruebas de visibilidad y notificaciones para el contador administrativo unificado.
- Pendiente de ejecutar compilación completa en el entorno con SDK disponible.
