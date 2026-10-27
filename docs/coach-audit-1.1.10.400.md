# Coach 1.1.10.400 — build 1116

## Error exacto

Validación 40551432578, revisión ddd9421266767289067da843ad0817317eef0be2,
trabajo installed-spanish (122373276176). El script
tools/verify-draft-rival-names-device.py:22 exigía el texto `OK (12 tests)`.
AndroidJUnitRunner terminó con `OK (14 tests)` después de agregar las dos
regresiones de títulos rápidos. El AssertionError del verificador bloqueó
la auditoría española y el historial de overlays que se ejecutan después.
Los fallos de build-and-release y workflow-report son consecuencias de ese
bloqueo, no otros errores de compilación. Los otros trabajos de validación,
incluidas las 16 partes de secuencias nativas, aprobaron esa revisión.

## Corrección

- tools/verify-draft-rival-names-device.py obtiene los métodos @Test del archivo
  de la revisión y exige que todos terminen correctamente en la clase esperada.
  Comprueba el resumen, el cierre normal, los nombres únicos, el número declarado
  y el estado individual. Rechaza pruebas faltantes, sustituidas, duplicadas,
  fallidas u omitidas y errores del proceso. No basta encontrar cualquier `OK`.
- tools/test_draft_rival_device.py cubre el conjunto actual, futuras adiciones,
  subconjuntos antiguos, nombres incorrectos, duplicados, omisiones, fallos,
  cierres incompletos y errores del proceso.
- .github/workflows/fix-error.yml ejecuta estas regresiones antes de compilar y
  añade espanol-instalado con los cuatro scripts del trabajo original: borrado
  de cuenta, títulos OCR, auditoría española e historial flotante. Conserva sus
  artefactos; ningún paso fallido se convierte en aprobado.
- .github/workflows/build-apk.yml incluye la regresión del verificador antes de
  la compilación completa. Este cambio se envía primero solo en fix-error.
- app/build.gradle.kts incrementa versión/build. No cambia el reconocimiento.

## Evidencia

Se descargó el artefacto real spanish-device-audit (12758610827) de la ejecución
fallida. Su archivo draft-rival-name-instrumentation.txt contiene los 14 métodos
aprobados. El verificador nuevo valida ese registro completo; el literal antiguo
no coincide, reproduciendo la causa del falso fallo.

Ocho regresiones Python del verificador y diez pruebas de auditoría de secuencias
aprobadas localmente. git diff --check aprobado. Las pruebas Android de la nueva
revisión deben ejecutarse en Actions, pues el entorno local carece de SDK.
La ejecución nueva no se declara aprobada antes de recibir su resultado.

Las pruebas instaladas son de emulador/imágenes; no certifican latencia física
en Red Magic, POCO ni otros teléfonos. main queda intacta y la corrección debe
aprobar primero fix-error antes de promoverse a coach-validacion.
