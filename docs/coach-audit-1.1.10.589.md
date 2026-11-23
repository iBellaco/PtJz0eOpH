# Coach 1.1.10.589 (1305) · Descarga estable de dependencias Robolectric

## Problema corregido

La validación de `fix-error` podía fallar antes de ejecutar las regresiones de
la aplicación cuando Robolectric intentaba descargar `android-all-instrumented`
desde `repo1.maven.org` y Maven Central respondía HTTP 429.

## Cambio realizado

- Se configuró el resolvedor runtime de Robolectric para usar el endpoint
  canónico `repo.maven.apache.org` de Maven Central.
- Se incrementó la versión de depuración a `1.1.10.589` (build `1305`).

## Archivos modificados

- `app/build.gradle.kts`
- `docs/coach-audit-1.1.10.589.md`

## Verificaciones

- Las suites Python de `.github/scripts` pasaron: 87 pruebas.
- Las suites Python de `tools` pasaron: 55 pruebas.
- La compilación Android local no pudo ejecutarse porque el entorno no tiene
  Gradle 9.3.1 descargado y la conexión al servidor de Gradle fue rechazada.
- La ejecución CI previa del mismo conjunto de regresiones pasó; la ejecución
  fallida se debió únicamente al HTTP 429 de Maven Central.
