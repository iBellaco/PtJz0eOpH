# Coach 1.1.10.408 — build 1124

## Fallo observado

La validación 40690459876 falló en catalog-validation, trabajo 122823769040.
test_known_full_release_failures_are_in_the_isolated_plan exigía las secuencias
sequence-0-enemy-first y sequence-42-ally-first en fix-error.yml. La simplificación
del flujo para patrocinio había eliminado sus trabajos instalados. Los servicios
de cuentas y permisos de la propuesta #171 sí pasaron; la compilación Android no
alcanzó a ejecutarse por el fallo de la comprobación previa.

## Corrección

- .github/workflows/fix-error.yml recupera la matriz instalada de regresiones OCR
  con la entrada manual scanner_regressions, desactivada por defecto. Incluye los
  mismos casos, compilación release de prueba, emulador y evidencias originales.
- Añade catalog-preflight con las mismas comprobaciones previas de validación;
  las pruebas dirigidas dependen de ese resultado para detectar antes este fallo.
- Las aserciones de la auditoría y las pruebas completas permanecen intactas.
- app/build.gradle.kts incrementa la versión.
- ModeratorRequestsDialog.kt y sponsor_review.xml de los tres directorios de
  idioma: el texto de anuncio pendiente usa un recurso traducido; la auditoría
  local completa detectó el literal español que habría bloqueado el siguiente paso.

## Verificación

Los diez casos de test_scanner_sequence_audit.py pasaron localmente. Se comprobó
la sintaxis YAML y la condición manual de la matriz opcional. La ejecución nueva
se inicia en fix-error; el propietario comunicará el resultado antes de integrar
en coach-validacion. No se fusiona en main ni se mantiene monitoreo continuo.

También pasaron localmente las demás comprobaciones de catalog-validation:
recursos, contenido, verificadores de dispositivos y secuencias, catálogos
generados y traducción. Tras corregir el recurso pendiente, la auditoría portuguesa
terminó con salida 0 y cero hallazgos de español en 2617 apariciones de literales.
