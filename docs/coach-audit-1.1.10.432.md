# Auditoría de Coach 1.1.10.432

## Cambio incluido

Las ejecuciones del arreglo pueden avanzar en paralelo. Una nueva ejecución ya
no cancela automáticamente otra ejecución activa del mismo flujo.

## Verificaciones

- `git diff --check`
- Revisión de la configuración de concurrencia del flujo de validación del arreglo.
