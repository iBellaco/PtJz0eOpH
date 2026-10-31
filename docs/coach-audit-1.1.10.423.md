# Auditoría Coach 1.1.10.423

## Corrección

- Se asignó un ciclo de vida independiente a cada ventana Compose del asistente flotante.
- Se liberan explícitamente la composición y los propietarios de ciclo de vida al ocultar o retirar el overlay.
- La ventana interactiva conserva una configuración táctil estable y la ventana auxiliar de depuración permanece no táctil.
- Se añadió recuperación visible para cancelaciones, tokens inválidos y `SecurityException` de MediaProjection en Android 14+.

## Verificación

- `versionCode`: 1139.
- `versionName`: `1.1.10.423`.
- `git diff --check` sin errores.
- La compilación local no pudo iniciarse porque el entorno no tenía descargada la distribución de Gradle y la conexión al servidor fue rechazada.
