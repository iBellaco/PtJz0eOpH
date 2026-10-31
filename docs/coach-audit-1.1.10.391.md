# Coach 1.1.10.391 — build 1107

## Evidencia de validación

Run 40320950303, revisión 820478ff (mismo árbol que fix-error 8db6c890).
Catálogo, seguridad/autenticación, compilación, pruebas generales y 14 de los
16 grupos de secuencia nativa aprobaron. Los fallos primarios fueron:

- Grupo 1, sequence-0-enemy-first, 1920×869: ConcurrentModificationException
  después de reconocer los nueve títulos, antes de registrar el décimo visual.
- Grupo 15, sequence-42-ally-first, 1920×863: en el paso 7 faltó Vex (slot 8).
  El diagnóstico independiente de atlas, escena y recorte sí devolvió VEX.
  En el paso 8 apareció. No se atribuye a una imagen ilegible.

La auditoría agregada y entrega fallaron como consecuencia. El test capturaba
solo nombre/mensaje de la excepción: la traza interna original NO se conservó.
Por ello no se afirma que los registros demuestren el punto exacto de la carrera.

## Corrección y protección

Se encontró iteración directa de SnapshotStateList en OCR y clasificación de
retratos mientras el sincronizador reemplaza el catálogo. Se añade una lectura
inmutable O(1), usada por cada análisis; índice y clasificación de retratos
comparten la misma instantánea. La selección regional y carga inicial publican
clear/addAll atómicamente. La regresión interrumpe deliberadamente una iteración
con el reemplazo del catálogo y exige conservar los 142 títulos, incluido VEX.

Las excepciones internas de OCR ya no se presentan como análisis exitoso.
La instrumentación conserva traza completa, candidatos por fotograma y errores
de OCR. Esto permite separar una lectura óptica ausente de una interrupción o
fallo de confirmación. No se relaja la exigencia de dos fotogramas ni se aceptan
valores esperados como lecturas.

Fix-error incorpora ambos casos completos en release en trabajos separados,
sin quitar los cinco casos previos. Antes el caso 42 completo solo se ejecutaba
en debug; el pase preliminar release no cubría el paso 7. Se guardan los informes
individuales con nombres distintos para no sobrescribir fallos anteriores.

## Verificación

13 pruebas Python de secuencia/informes pasan; YAML y plan verificados localmente.
Kotlin y OCR nativo quedan pendientes de Actions (sin SDK/emulador local).
No se presenta esta corrección como aprobada antes del resultado nativo.
Las pruebas usan imágenes/emulador, no teléfonos físicos. Main y coach-validacion
no se modifican en esta intervención; no se reactiva la vigilancia automática.

Archivos: WildRiftRepository.kt, DraftVisionScanner.kt, LiteRTVisionClassifier.kt,
ChampionNameResolverCoverageTest.kt, DraftSequenceMultiDeviceInstalledTest.kt,
targeted-sequence-plan.json, fix-error.yml, verify-draft-sequence-device.py,
test_scanner_sequence_stream.py, test_scanner_sequence_audit.py,
app/build.gradle.kts y este informe.
