# Coach 1.1.10.509 — Corrección de secuencia OCR y perfiles exactos de calibración

## Correcciones y modificación

- La validación completa falló en `native-name-sequence (9)`: el caso
  `sequence-52-ally-first` completó las nueve lecturas y confirmó el décimo
  retrato, pero la comprobación posterior de una captura real de 854 × 480 agotó
  su límite fijo de 60 segundos.
- La comprobación de capturas reales ahora usa el mismo límite acotado de 120
  segundos que ya se aplicaba al OCR secuencial de capturas de hasta 960 píxeles
  de ancho. Las demás dimensiones conservan 60 segundos.
- Se añadieron seis perfiles físicos de calibración como valores iniciales solo
  con coincidencia exacta de fabricante, modelo y dimensiones de captura.
- Las calibraciones guardadas manualmente conservan prioridad. Los perfiles no
  se comparten entre modelos que tengan la misma resolución.
- Se incorporó al respaldo el perfil Samsung SM-S948B de 3120 × 1440, sin datos
  de cuenta ni identidad del usuario que aportó la medición.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/androidTest/java/com/example/DraftSequenceMultiDeviceInstalledTest.kt`
- `app/src/main/java/com/example/service/screen/DraftVisionScanner.kt`
- `app/src/main/java/com/example/service/screen/MeasuredDeviceCalibrationDefaults.kt`
- `app/src/main/java/com/example/service/screen/VisionCalibrationConfig.kt`
- `app/src/test/java/com/example/DeviceDraftProfileTest.kt`
- `docs/scanner-calibration-backup.json`
- `docs/scanner-device-validation.md`
- `docs/draft-recognition-contract.md`
- `docs/REGLAS_VERSIONES_Y_COMPATIBILIDAD_MULTIDISPOSITIVO.md`
- `AGENTS.md`

## Versión y verificaciones

- Versión `1.1.10.509`, build `1225`.
- Verificación de sintaxis del respaldo JSON.
- Pruebas unitarias dirigidas `DeviceDraftProfileTest` y
  `CaptureCoordinateSpaceTest` superadas.
- Compilación de la aplicación y del código de prueba instrumental Android
  superada con SDK 36.
- Quince comprobaciones de planificación, consolidación y paralelización de la
  secuencia superadas.
- La reproducción instalada de `sequence-52-ally-first` queda a cargo de la
  validación selectiva remota, porque requiere emulador Android.
