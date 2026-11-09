# Auditoría de corrección del auditor portugués de Coach 1.1.10.497

## Versión

- `versionName`: `1.1.10.497`
- `versionCode`: `1213`
- Alcance: corregir el falso fallo de la auditoría instalada de portugués.

## Diagnóstico

La ejecución de validación falló en `installed-audit` durante la revisión del
APK instalado en portugués. El catálogo y las superficies renderizadas habían
pasado, pero `tools/audit-portuguese-device.py` solo extraía el primer literal
de `SpanishUiResidue.pattern`. Al concatenarse el nuevo fragmento del patrón
con el patrón histórico, el fragmento aislado terminaba en un operador `|` y
el auditor coincidía con texto vacío, marcando todos los textos visibles como
residuo español.

## Corrección aplicada

- El auditor ahora reconstruye todos los literales Kotlin concatenados antes de
  convertir el patrón a la expresión compatible con Python.
- Se añadió una prueba de regresión que verifica la conservación de todos los
  fragmentos concatenados.
- No se modificó el catálogo de traducciones ni se relajó la detección de
  español; únicamente se corrigió la lectura del patrón.

## Archivos modificados

- `app/build.gradle.kts`
- `tools/audit-portuguese-device.py`
- `tools/test_portuguese_device_audit.py`

## Verificaciones

- `python3 -m unittest tools.test_portuguese_device_audit -v`: 11 pruebas
  correctas.
- Comprobación directa del patrón: no coincide con portugués válido y sí
  detecta frases españolas de control.
- `git diff --check`: correcto.

La auditoría instalada completa debe repetirse en la siguiente ejecución de
validación para confirmar el APK firmado con esta corrección.
