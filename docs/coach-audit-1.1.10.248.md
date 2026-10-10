# Auditoría Coach 1.1.10.248

- Versión: 1.1.10.248
- Build: 964
- Problema: el adaptador de geometría limitaba las coordenadas X calibradas a las posiciones predeterminadas, por lo que la vista mostraba un valor diferente al que usaba el escáner. Los selectores de objetivos estaban en una fila horizontal desplazable y varios controles no alcanzaban el tamaño táctil mínimo.
- Cambio: el escáner conserva las coordenadas X normalizadas y el diámetro configurados por el usuario en relaciones de aspecto distintas. Los objetivos de calibración ahora se muestran en filas visibles; las flechas, los pasos y los controles de tamaño tienen objetivos táctiles de al menos 48 dp y etiquetas para comprobación automatizada.
- Archivos modificados: `app/build.gradle.kts`, `app/src/main/java/com/example/service/screen/AdaptiveScreenLayoutEngine.kt`, `app/src/main/java/com/example/ui/components/LiteRTEngineViewerDialog.kt`, `app/src/main/java/com/example/ui/components/DraftCalibrationPanel.kt`, `app/src/test/java/com/example/TenthPickRegressionTest.kt`.
- Pruebas: se añadió una prueba que comprueba que los centros, las coordenadas individuales y los diámetros personalizados sobrevivan a capturas 20:9, 16:9, 4:3 y panorámicas. Ejecutar pruebas y auditorías de publicación con el flujo de validación antes de distribuir.
