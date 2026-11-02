# Coach 1.1.10.461 — build 1177

La ejecución 41511148481 vuelve a fallar en native-name-sequence (9),
sequence-52-ally-first, captura real draft-mel-nine.jpg a 854 × 480.
La secuencia preparada pasa; la captura real omite VI e impide confirmar Teemo.
Por tanto, las correcciones anteriores no habían resuelto este caso.

## Evidencia y cambio

El diagnóstico usa cajas OCR medidas en la captura original, incluida
Rect(91,160,123,168) del título O ORNN. Con ese contexto el OCR devuelve
VI dentro del objetivo central. La recuperación productiva usaba la escena
ampliada a 1920 píxeles, que no garantiza las mismas cajas de texto.

DraftNameAtlas.sceneTitles permite conservar ahora el tamaño original.
DraftVisionScanner incorpora esa alternativa al final de las recuperaciones,
solo para capturas menores de 1920 píxeles con candidatos aún pendientes y
fuera de las pasadas rápidas. Se filtran los donantes por títulos completos
en otras bandas calibradas y se reutiliza la exclusión de máscaras.
La identidad procede únicamente del recorte objetivo y continúa necesitando
confirmación en dos fotogramas. No se introducen nombres esperados ni modelos.

El flujo fix incluye ahora la secuencia completa sequence-52-ally-first con
sus tres capturas reales, además de las comprobaciones instaladas previas.
Antes, las pruebas instaladas de fix comprobaban otros casos de VI y no
reproducían el caso exacto que fallaba en validación.

## Verificación y límites

- Nueva prueba DraftNativeSceneTest: aprobada en JUnit/Robolectric 4.16.1,
  gráficos NATIVE y API 34, compilando DraftNameAtlas y DraftImagePreprocessor
  productivos contra el arnés local existente. Verifica dimensiones originales,
  máscaras, integridad de la fuente y transformación exacta del donante medido.
- Diez pruebas Python del grafo y diez de auditoría de secuencias aprobadas.
- La lectura ML Kit del caso exacto queda pendiente de la ejecución instalada
  del nuevo envío. La prueba geométrica local no demuestra por sí sola que el
  OCR haya quedado resuelto. No hay comprobaciones físicas nuevas.
- Se envía a fix; no se promueve a validación antes de conocer su resultado,
  ni se realiza monitoreo continuo.

Archivos: DraftNameAtlas.kt, DraftVisionScanner.kt, DraftNativeSceneTest.kt,
fix-error.yml, app/build.gradle.kts y este informe.
