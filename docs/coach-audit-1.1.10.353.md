# Coach 1.1.10.353 — build 1069

## Validación completa tras `fix-error`

El flujo aislado `fix-error` de la revisión `87a0442b` terminó correctamente
(ejecución 40152050066). Sus correcciones de lectura de títulos, líneas
aliadas observadas y dos fotogramas iniciales se incorporan a
`coach-validacion`, que alimenta la solicitud #163. No se fusiona `main`.

El flujo completo aún no se ha aprobado para esta revisión. Antes de ejecutarlo
se corrigen dos desajustes de sus comprobaciones:

- El resumen nativo exigía un solo fotograma en el paso inicial, mientras la
  prueba instalada aprobada ya exige dos. Ahora el resumen y su prueba exigen
  dos en todos los pasos.
- El gate rápido incluía `sequence-4-ally-first`, una simulación geométrica
  genérica de 640 × 360, no un modelo de teléfono. La matriz geométrica sigue
  completa, pero el plan OCR nativo selecciona solo perfiles de teléfonos;
  mantiene los índices originales para los escenarios P20 y Honor X8a que
  pasaron en `fix-error`.

Archivos modificados: `app/build.gradle.kts`, `.github/workflows/build-apk.yml`,
`tools/scanner-sequence-plan.py`, `tools/scanner-sequence-summary.py`,
`tools/test_scanner_sequence_audit.py` y este informe. Los cambios de
`fix-error` se conservaron sin alterar su revisión aprobada.

Comprobación local: pruebas unitarias de planificación/resumen OCR aprobadas.
La ejecución Android completa, la firma/R8 y las pruebas de APK instalado
quedan pendientes del flujo de la solicitud #163. No se atribuye una prueba
física nueva a ningún modelo; Red Magic y POCO solo cuentan con las pruebas
previas comunicadas por el propietario.
