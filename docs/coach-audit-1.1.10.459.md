# Coach 1.1.10.459 — build 1175

## Fallo confirmado

La validación 41478647937 conservó verdes todos los escenarios salvo el
fragmento `sequence-52-ally-first` del artefacto `native-name-sequence (9)`.
En la captura `draft-mel-nine.jpg`, el OCR detectó el título aliado `VI` como
un elemento dentro del recorte contextual, pero descartó la línea completa al
ver también texto usado como contexto de los títulos vecinos. Por eso faltaba
el noveno nombre y el décimo retrato no se habilitaba.

## Cambios

- `DraftVisionScanner.kt`: la recuperación contextual ahora evalúa los centros
  de los elementos OCR dentro del área objetivo y puede confirmar un único
  campeón completo detectado allí, aunque la línea devuelta por ML Kit contenga
  texto contextual vecino.
- `AllyDraftNameReader.kt`: añade una lectura estricta de elementos
  contextuales; solo devuelve un campeón cuando hay exactamente un candidato
  completo y no usa texto vecino como identidad.
- `AllyDraftNameReaderTest.kt`: añade la regresión del caso contextual con
  `ORN`, `VI` y `ORNH`, confirmando únicamente `VI`.
- `app/build.gradle.kts`: versión 1.1.10.459, build 1175.

## Comprobaciones

- Prueba unitaria del lector de títulos y su regresión contextual.
- Validación dirigida del artefacto nativo que falló, sin volver a ejecutar los
  escenarios que ya estaban verdes.
- No se consideran aprobados teléfonos físicos ni escenarios no incluidos en
  la ejecución dirigida.
