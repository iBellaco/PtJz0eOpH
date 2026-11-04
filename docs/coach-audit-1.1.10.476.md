# Coach 1.1.10.476 — build 1192

Corrección destinada a producción, pendiente de validación instalada.

- El filtro conserva la columna rival después de dos observaciones compatibles;
  cambios de retrato no desplazan una geometría ya confirmada. Su reinicio sigue
  ligado al cambio de viewport, dimensiones o calibración.
- La apertura automática de la build con diez campeones únicos detiene Auto-Scan
  y solicita una sola persistencia, según la nueva indicación del propietario.
- Vaciar cierra la ficha y el selector; cada sesión tiene una generación para
  descartar resultados tardíos en el Hub. El botón del overlay pasa de 28 a 48 dp.
- El Hub principal restablece cinco puestos vacíos con sus líneas por equipo.
- Se añaden regresiones de geometría, apagado al abrir build y reinicio de sesión
  a la comprobación acotada existente. No se duplican ejecuciones completas.

Archivos: `DraftGeometryStability.kt`, `DraftRingAlignment.kt`, `DraftVisionScanner.kt`,
`OverlayState.kt`, `FloatingOverlayContent.kt`,
`FloatingDraftCoachView.kt`, `MetaAndDraftScreen.kt`, `DraftAnalysisTab.kt`,
`DraftGeometryStabilityTest.kt`, `DraftAutoStopTest.kt`, `DraftSessionLifecycleTest.kt`,
`app/build.gradle.kts`, `.github/workflows/fix-error.yml`, el contrato de reconocimiento
y este informe.

La captura Oppo compartida no permite verificar el recorte efectivo de MediaProjection
ni reproducir toda la secuencia. Quedan pendientes pruebas instaladas y físicas
en ese dispositivo; no se afirma corregida toda su alineación a partir de una imagen.

Comprobaciones locales: `git diff --check` y 14 pruebas estructurales del flujo
correctas. Gradle configura el proyecto, pero las pruebas Android no pueden
ejecutarse aquí: no está instalado el SDK. Las tres clases de regresión se
ejecutarán en la única comprobación de `fix-error` asociada a este cambio.
