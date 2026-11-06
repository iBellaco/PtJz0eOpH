# Coach 1.1.10.483 — build 1199

## Ajuste de entrega

- Se eliminó la modalidad de versión de prueba del proceso del proyecto.
- La compilación, validación, firma y entrega usan únicamente la versión
  numérica de producción y la identidad de firma persistente.
- El flujo ya no acepta `test_build`, no añade sufijos `-prueba` y no genera
  APK/ZIP alternativos de prueba.
- La documentación de versiones queda alineada con este único proceso.

## Verificación

- Se actualizaron las comprobaciones del flujo para exigir exclusivamente la
  entrega de producción.
- `git diff --check` y las pruebas de scripts de configuración deben ejecutarse
  antes de la validación Android completa.
- La compilación Android de esta revisión queda pendiente de la validación
  remota obligatoria.
