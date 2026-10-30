# Auditoría Coach 1.1.10.425

## Corrección

- Se eliminó la concurrencia exclusiva del flujo de corrección para permitir ejecuciones paralelas independientes.
- Una nueva ejecución ya no cancela automáticamente otra ejecución activa de la misma rama.

## Verificación

- `versionCode`: 1141.
- `versionName`: `1.1.10.425`.
- La configuración YAML conserva permisos y trabajos sin cambios funcionales.
