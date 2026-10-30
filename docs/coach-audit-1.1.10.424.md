# Auditoría Coach 1.1.10.424

## Corrección

- La regresión JVM invoca `gradlew` mediante `bash` para funcionar cuando el proyecto está en almacenamiento Android montado con `noexec`.
- Se revisaron los scripts Python de validación y no se encontraron otras ejecuciones directas de `gradlew`.

## Verificación

- `versionCode`: 1140.
- `versionName`: `1.1.10.424`.
- `git diff --check` sin errores.
