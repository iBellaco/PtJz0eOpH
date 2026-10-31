# Auditoría Coach 1.1.10.250

- Build: 966
- Cambio: el calibrador del visor muestra solamente «Columna Aliado X» y «Columna Rival X». Los controles arriba, abajo, izquierda, derecha, aumentar, reducir y pasos modifican la columna elegida y conservan las separaciones verticales entre los cinco espacios.
- Corrección técnica: los recortes de diagnóstico y de reconocimiento visual usan la posición y el diámetro individual configurados para cada espacio. La calibración se guarda en el almacenamiento local existente.
- Archivos modificados:
  - `app/src/main/java/com/example/ui/components/LiteRTEngineViewerDialog.kt`
  - `app/src/main/java/com/example/service/screen/VisionCalibrationConfig.kt`
  - `app/src/main/java/com/example/service/screen/DraftVisionScanner.kt`
  - `app/src/test/java/com/example/TenthPickRegressionTest.kt`
  - `app/build.gradle.kts`
- Verificaciones: pruebas de regresión para movimiento y tamaño por columna y para el recorte resultante; compilación release R8, comprobación de firma e instalación pendientes de CI.
