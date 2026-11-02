# Coach 1.1.10.445 — build 1161

## Base y alcance

Se parte del contenido de coach-validacion que incorpora 1.1.10.444. No se
descartan sus cambios. La segunda captura del propietario corresponde a
1.1.10.442: demuestra el desplazamiento anterior, no una prueba fallida de
1.1.10.444. No se introducen coordenadas por marca/modelo. Las capturas visibles
tienen aproximadamente 1280×587 y 1280×590; no se confunden estas dimensiones
con la resolución física o la resolución efectiva de MediaProjection.

## Problemas encontrados y cambios

- La vista previa anterior salía si la proyección aún no estaba disponible,
  sin observar la disponibilidad posterior. ScreenCaptureManager publica ahora
  su disponibilidad; FloatingOverlayContent vuelve a solicitar una actualización
  al recibirla, cambiar las dimensiones, expandir/contraer o terminar una lectura
  manual. Espera brevemente a que el layout quede medido.
- DraftGeometryPreview limita cada solicitud a dos imágenes nuevas, con espera
  de adquisición limitada, liberación incluso al cancelar y sin bucle permanente.
  Ocultar el visor o activar autoescaneo cancela la vista previa.
- DraftVisionScanner permite refrescar la geometría en esas dos imágenes, sin
  reutilizar obligatoriamente la caché de dos segundos. El autoescaneo conserva
  su cadencia habitual. No se sobrescriben las calibraciones manuales ni se
  alteran las reglas de confirmación de títulos y baneos.
- CaptureCoordinateSpace centraliza la transformación uniforme de captura.
  ScannerDebugOverlay la invierte para dibujar en pantalla las coordenadas
  efectivamente empleadas por OCR y retratos, sin estirar cada eje por separado.
  Las bandas de baneos y telemetría respetan el viewport observado. Las máscaras
  del texto se publican en el mismo espacio de captura en que se han dibujado.
- Los límites de exclusión de la ventana son observables: el visor ya no depende
  solo del rectángulo recibido cuando se creó su composición. El visor pasivo
  usa toda la superficie, incluidos los insets, manteniendo NOT_TOUCHABLE.

## Archivos

- app/build.gradle.kts
- app/src/main/java/com/example/service/FloatingAssistantService.kt
- app/src/main/java/com/example/service/FloatingOverlayContent.kt
- app/src/main/java/com/example/service/screen/ScreenCaptureManager.kt
- app/src/main/java/com/example/service/screen/DraftGeometryPreview.kt
- app/src/main/java/com/example/service/screen/DraftVisionScanner.kt
- app/src/main/java/com/example/service/screen/CaptureCoordinateSpace.kt
- app/src/main/java/com/example/ui/components/ScannerDebugOverlay.kt
- app/src/test/java/com/example/DraftGeometryPreviewTest.kt
- app/src/test/java/com/example/DraftPreviewAlignmentTest.kt
- app/src/test/java/com/example/CaptureCoordinateSpaceTest.kt
- app/src/test/java/com/example/DraftRingAlignmentTest.kt
- app/src/test/java/com/example/PocoDebugCaptionLayoutTest.kt
- .github/workflows/fix-error.yml

## Verificación

Comprobaciones locales correctas: git diff --check, recursos de idiomas, 10
pruebas de planes de validación, 15 de reutilización de APK, 6 de recursos,
2 de informes multidispositivo y 1 de informes de fallos JUnit (34 en total).

Pruebas Android añadidas a la ejecución existente, sin crear otro flujo:
adquisición limitada/cancelación, segunda geometría antes de dos segundos,
respeto de calibración manual, transformación en varias proporciones, límites
de ventana reactivos y renderizado de puestos/baneos con letterboxing. Se
conservan las regresiones reales de S26 y se amplía la matriz sintética con
1280×590 además de 1280×587 y otras proporciones.

El intento local de testDebugUnitTest no pudo arrancar: la descarga de Gradle
9.3.1 terminó en Connection refused. Esas pruebas Android están pendientes de
ejecución remota; no se presentan como aprobadas. El entorno tampoco dispone
de SDK ni de teléfonos físicos. No se generó APK final en esta intervención.
La nueva captura adjunta no está disponible como archivo de fixture local;
las pruebas sobre el recurso S26 existente y sobre escenas sintéticas no son
pruebas de esa nueva captura ni de los teléfonos físicos de los testers.

## Entrega

Una sola actualización en fix-error. Esperar el aviso del propietario sobre su
resultado antes de promover a coach-validacion. Mantener una única solicitud
de coach-validacion hacia main; no fusionar main automáticamente ni vigilar
continuamente las ejecuciones.
