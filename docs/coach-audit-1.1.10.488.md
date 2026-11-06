# Coach 1.1.10.488 — Validación selectiva y recuperación de secuencias nativas

## Diagnóstico

- La última validación completa terminó correctamente en las suites de catálogo,
  seguridad, escáner, interfaz, compilación y auditorías instaladas.
- El rojo se originó únicamente en `native-name-sequence (10)`: el proceso de
  instrumentación alcanzó el límite interno anterior de 55 minutos antes de
  publicar su evidencia. Los trabajos de resumen y publicación fallaron como
  consecuencia de ese shard incompleto.

## Cambios implementados

- `fix-error` ahora clasifica automáticamente los archivos modificados y ejecuta
  solo el alcance seguro relacionado: draft/escáner, interfaz,
  patrocinadores/economía o validación completa.
- Se añadió selección manual de alcance para correcciones dirigidas.
- Se añadió una ejecución dirigida para repetir un único shard nativo y, cuando
  se proporciona el identificador de una validación anterior, reconstruir su
  matriz completa sin repetir los demás shards.
- La validación completa conserva todas las suites y ahora permite hasta 75
  minutos por shard nativo, con un presupuesto interno de 4200 segundos para
  evitar perder evidencia válida por el límite anterior.
- Se habilitó la caché de npm en los workflows que reinstalaban las dependencias
  de servicios en cada job.
- `AGENTS.md` documenta que la selección aplica solo a `fix-error`; antes de
  producción `coach-validacion` debe ejecutar la validación completa.

## Verificaciones locales

- Pruebas del clasificador de alcance: 6 casos superados.
- Compilación sintáctica de los scripts Python superada.
- Validación YAML de todos los workflows superada.
- `git diff --check` superado.

La compilación Android y la matriz de emuladores deben comprobarse en GitHub
Actions, donde están disponibles el SDK, los emuladores y la firma de release.
