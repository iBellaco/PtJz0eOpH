# Coach 1.1.10.570 (1286) · Retratos del juego y posición de los baneos

## Informe del Samsung SM-G998B

Android 15 (API 35), pantalla de 2400 × 1080, captura de 1920 × 864 y viewport completo. El adjunto de 1280 × 576 tenía 81 ms de antigüedad. SHA-256 `25e5a62c46a699653f3ca5cfd8ff8dbfc4930333c411a4c100922985aab94d49`. El informe indicaba «sin captura» y ambos ajustes manuales de baneos estaban desactivados.

| Coordenadas normalizadas del informe | Aliados | Rivales |
| --- | --- | --- |
| X de los cinco puestos | 0.072 | 0.959 |
| Y de los puestos, de arriba abajo | 0.188, 0.324, 0.458, 0.591, 0.725 | 0.188, 0.324, 0.457, 0.590, 0.722 |
| Diámetros de los puestos | 0.11, 0.11, 0.11, 0.11, 0.105 | 0.11 en los cinco |
| X de los baneos, de izquierda a derecha | 0.028, 0.064, 0.100, 0.136, 0.172 | 0.826, 0.863, 0.900, 0.937, 0.974 |
| Y y diámetro de los baneos | 0.052 y 0.060 | 0.052 y 0.060 |
| X del quinto círculo superior | 0.168 | 0.974 |

El diámetro global era 0.11. La referencia superior tenía Y de 0.045 y diámetro de 0.072. Estas cifras documentan la configuración que produjo el fallo. La posición física de los círculos en ese modelo sigue pendiente de medición.

El informe registró a Zoe, Rell, Yasuo, Vayne y Lee Sin frente a Aurora, Caitlyn, Thresh, Vi y Sett. Reconoció a Lee Sin por imagen y a los otros nueve por texto. El resumen contaba cero puestos y veinte retratos cargados. En los baneos aliados registró Yuumi (0.92), Kai’Sa (0.95), Kai’Sa (0.95), Nocturne (0.93) y Cho’Gath (0.90). En los rivales registró Smolder (0.91), Syndra (0.93), Darius (0.90), Sylas (0.87) y Akali (0.90). La captura por sí sola deja abierta la identidad de los dos baneos en conflicto.

## Corrección

- El visor en vivo y los adjuntos dibujan contornos alrededor del retrato del juego. Las etiquetas del adjunto van encima de cada puesto. Se retiraron los retratos del catálogo que se pintaban por segunda vez y la fila adicional de anillos blancos sobre los baneos.
- Los informes de coordenadas y diagnóstico comparten la captura, geometría e identidades usadas para dibujarla. El fotograma de respaldo conserva sus propias mediciones. Guardar las coordenadas conserva el ajuste visual y los perfiles antiguos con los valores de fábrica pueden volver a alinearse. Los ajustes manuales conservan su prioridad.
- El seguimiento conserva el índice de cada círculo al ocultarse parte de una fila ya localizada. Dentro del mismo equipo, dos candidatos nuevos con igual confianza requieren más evidencia. Un cambio de puesto de una identidad confirmada requiere dos lecturas claramente mejores. Ambos equipos pueden banear al mismo campeón.

## Archivos

- `BanDetection.kt`, `LiteRTVisionClassifier.kt`, `BannedPortraitScanner.kt`, `VisionCalibrationConfig.kt`, `DraftVisionScanner.kt` y `CalibrationEvidenceImage.kt` en el escáner.
- `ScannerDebugOverlay.kt`, `CalibrationControls.kt`, `DeviceDiagnosticButton.kt` y `FloatingAssistantService.kt` en la interfaz y el servicio flotante.
- `BannedPortraitScannerTest.kt`, `CalibrationEvidenceImageTest.kt`, `DraftPreviewAlignmentTest.kt` y `PocoDebugCaptionLayoutTest.kt` en pruebas.
- `docs/draft-recognition-contract.md` recoge el comportamiento de los contornos y la confirmación por equipo.
- `app/build.gradle.kts` y `.github/workflows/fix-error.yml` para la versión y las pruebas de la captura.

## Verificación

- Pasaron 58 pruebas locales de Android en ocho suites. Se comprobaron los píxeles del visor en vivo y del adjunto, los dos baneos aliados con igual confianza, los baneos iguales entre equipos, la recuperación tras ocultar parte de la fila y la persistencia de la alineación. La regresión con la captura POCO 7457 pasó en su tamaño original y al reescalarla.
- Pasaron 28 comprobaciones de las herramientas de validación. Se comprobaron los recursos localizados, la sintaxis del archivo de validación y el diff.
- La confirmación física en el Samsung SM-G998B requiere una nueva captura con esta versión, con el visor encendido y apagado.
