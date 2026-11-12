# Auditoría Coach 1.1.10.526

- `versionCode`: `1242`.
- `versionName`: `1.1.10.526`.
- Rama de trabajo: `fix-error`.

## Evidencia visual de calibración

- El escáner conserva una copia acotada del último fotograma que ya procesó,
  con una frecuencia limitada para no iniciar capturas adicionales ni aumentar
  el consumo de batería.
- Al enviar las coordenadas se dibujan sobre ese fotograma los recortes de los
  cinco puestos aliados, los cinco rivales, los baneos y los círculos superiores
  con la configuración exacta que acompaña al informe. La imagen se comprime a
  un tamaño controlado y se adjunta solo al informe visible para el revisor.
- El texto del informe incluye dimensiones, antigüedad del fotograma y una
  huella SHA-256 para comprobar que la imagen no cambió después de enviarse.
  Si no había un fotograma procesado, el informe lo declara en lugar de crear
  una captura ficticia.
- El revisor puede ampliar la evidencia y descargarla como JPG en
  `Descargas/Coach`. La descarga requiere la sesión de revisor y conserva el
  borrado manual existente.

## Retención y privacidad

- Los informes de coordenadas (`COORDENADAS`, `CALIBRACION` y `CALIBRAÇÃO`) ya
  no caducan ni se purgan automáticamente. Permanecen disponibles hasta que el
  revisor los elimine de forma manual.
- La imagen se limita al fotograma del juego que el usuario envió al pulsar
  «Guardar y enviar coordenadas»; no se solicitan permisos de captura nuevos.
  El adjunto puede contener nombres visibles en la partida, por lo que se
  mantiene dentro del flujo restringido de revisión.
- El adjunto se guarda una sola vez en el campo de fotografías del informe para
  evitar duplicar Base64 en el texto de dispositivo y reducir el tamaño de
  sincronización.

## Archivos principales

- `DraftVisionScanner.kt`: retención temporal del último fotograma procesado.
- `CalibrationEvidenceImage.kt`: superposición geométrica, compresión y hash.
- `CalibrationControls.kt`: adjunto, metadatos y evidencia de fuente al enviar.
- `FeedbackRepository.kt`: retención manual de coordenadas y sin duplicación.
- `AdminSupportReportsDialog.kt`: indicador «Sin caducidad» y descarga JPG.
- `translations_pt.json`: textos de descarga y retención en portugués.
- `CalibrationReportTest.kt` y `CalibrationEvidenceImageTest.kt`: pruebas de
  sobre, política de retención y codificación de la evidencia.

## Verificaciones

- `python3 -m json.tool app/src/main/assets/translations_pt.json`: correcto.
- `bash tools/audit-portuguese.sh`: correcto, sin hallazgos en portugués.
- `git diff --check`: correcto.
- `./gradlew :app:testDebugUnitTest --tests com.example.CalibrationReportTest --tests com.example.CalibrationEvidenceImageTest`: no ejecutable en este entorno porque no está instalado el SDK de Android; la comprobación queda para la ejecución oficial.
