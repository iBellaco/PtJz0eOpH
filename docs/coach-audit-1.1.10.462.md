# Coach 1.1.10.462 — build 1178

La ejecución fix 41523421389 falla antes de iniciar la secuencia instalada:
`AssertionError: La partición no puede estar vacía`. El caso
sequence-52-ally-first pertenece a la partición 0 del plan reducido incluido
en fix, aunque pertenece a la 9 de la matriz completa de validación.
La configuración introducida en la intervención anterior utilizaba 9.

Se corrige COACH_SEQUENCE_SHARD a 0 manteniendo el filtro del caso exacto y
el modo completo (diez etapas y las tres capturas reales). No se modifica
la lógica de reconocimiento. La versión se incrementa según AGENTS.md.

Se añade una prueba del flujo que cruza sus parámetros con el plan reducido
y exige que todos los casos solicitados estén en la partición elegida.
Esta prueba ya se ejecuta en el preflight, antes de compilar o arrancar Android.

Comprobaciones locales: 11 pruebas del grafo aprobadas; la nueva regresión
rechaza la configuración anterior con partición 9 y acepta la corregida.
`git diff --check` sin errores.

Las dos pruebas instaladas previas de Vi pasaron en la ejecución fallida.
La secuencia nueva no llegó a ejecutarse, por lo que aún no se afirma que la
recuperación OCR del caso real haya pasado. No se promueve a validación ni se
realiza monitoreo continuo.

Archivos: .github/workflows/fix-error.yml,
.github/scripts/test_parallel_validation.py, app/build.gradle.kts y este informe.
