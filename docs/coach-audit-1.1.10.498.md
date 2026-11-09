# Auditoría de ejecución obligatoria de fix-error de Coach 1.1.10.498

## Versión

- `versionName`: `1.1.10.498`
- `versionCode`: `1214`
- Alcance: evitar que el preflight obligatorio quede omitido silenciosamente
  en la validación selectiva.

## Diagnóstico

La ejecución de `fix-error` terminó en rojo sin un job fallido ni logs de error.
El detector de alcance terminó correctamente, pero `catalog-preflight` no fue
creado en el grafo y la compilación dependiente quedó omitida. El resultado no
permitía distinguir una validación real de una ejecución incompleta.

## Corrección aplicada

- `catalog-preflight` ahora usa una condición explícita con `always()` y exige
  que `validation-scope` haya terminado correctamente.
- Se añadió una prueba de integridad del workflow para impedir que el
  preflight obligatorio vuelva a perder esa condición.
- No se relajaron las comprobaciones: si el detector falla, el preflight sigue
  sin ejecutarse; si termina correctamente, el preflight debe aparecer y
  reportar sus errores reales.

## Archivos modificados

- `.github/workflows/fix-error.yml`
- `.github/scripts/test_parallel_validation.py`
- `app/build.gradle.kts`

## Verificaciones

- Prueba de integridad del grafo y dependencias del workflow.
- `git diff --check`.
- La siguiente ejecución de `fix-error` debe confirmar que el preflight y las
  suites seleccionadas aparecen en el grafo antes de integrar la corrección.
