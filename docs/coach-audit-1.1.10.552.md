# Coach 1.1.10.552 — Evidencia de calibración aislada y recomendaciones compactas

Versión: 1.1.10.552; versionCode: 1268.

## Cambios

- El envío de coordenadas ya no activa ni modifica el visor en vivo del
  dispositivo. El estado que tenía el usuario se conserva aunque la sesión no
  esté verificada, se alcance el límite diario o falle la conexión.
- La imagen adjunta genera una capa de auditoría independiente: muestra las
  cajas y círculos de calibración, los retratos locales disponibles y los
  nombres confirmados, sin añadir una cabecera que tape la fila superior.
- El informe de coordenadas identifica la fuente de captura, el tamaño y
  viewport alineados, el perfil geométrico, las identidades representadas y la
  cantidad de retratos cargados. También aclara que el visor se simuló solo en
  el adjunto y no se modificó en el remitente.
- El diagnóstico del dispositivo incorpora orientación, viewport, fuente de
  calibración y estado actual del visor, además de conservar las métricas de
  batería, memoria, captura y procesamiento.
- Las recomendaciones del draft tienen una vista compacta de hasta dos
  frases y un límite de caracteres en las tarjetas y el overlay. El análisis
  completo sigue disponible al abrir el detalle.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/ui/components/CalibrationControls.kt`
- `app/src/main/java/com/example/service/screen/CalibrationEvidenceImage.kt`
- `app/src/main/java/com/example/ui/components/DeviceDiagnosticButton.kt`
- `app/src/main/java/com/example/util/DraftRecommendationText.kt`
- `app/src/main/java/com/example/ui/components/DraftAdviceSection.kt`
- `app/src/main/java/com/example/ui/screens/DraftAnalysisTab.kt`
- `app/src/main/java/com/example/service/FloatingDraftCoachView.kt`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/test/java/com/example/CalibrationEvidenceImageTest.kt`
- `app/src/test/java/com/example/DraftRecommendationTextTest.kt`

## Verificaciones

- Se añadieron pruebas unitarias dirigidas para el recorte de recomendaciones
  y la codificación de la evidencia visual.
- `./gradlew :app:compileDebugKotlin --offline`: correcto.
- La suite JVM no pudo ejecutarse en este entorno porque el JRE disponible no
  incluye `javac`; queda pendiente en la integración con el JDK completo.
- Debe comprobarse en un dispositivo con overlay que el visor conserva su
  estado después de enviar, cancelar o fallar un informe y que la imagen
  adjunta muestra los retratos esperados.

## Texto para pruebas

Coach 1.1.10.552

- Enviar coordenadas ya no activa ni deja visible el visor en el dispositivo.
- El archivo adjunto incluye una vista de auditoría con las cajas de
  calibración, círculos y retratos confirmados para revisar la colocación.
- El informe indica el origen de la captura, el viewport utilizado, las
  identidades detectadas y cuántos retratos se pudieron cargar.
- El diagnóstico conserva los datos de batería, memoria, captura y escáner, y
  añade orientación, viewport, fuente de calibración y estado del visor.
- Las recomendaciones del draft se muestran resumidas y directas; el detalle
  táctico completo sigue disponible al abrir una recomendación.
