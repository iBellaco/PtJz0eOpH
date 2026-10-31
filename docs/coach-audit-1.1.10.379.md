# Coach 1.1.10.379 — build 1095

La ejecución 40251972297 aprobó las pruebas unitarias pero omitió VI en la
captura real draft-mel-nine.jpg a 1920×822, 1920×924 y 1920×889. Teemo quedó
bloqueado al faltar el noveno nombre. La recuperación anterior no está validada.

Se detectó que el recorte binario ajustado a tinta colocaba trazos negros sobre
el borde de una página negra. Ahora cada tile contiene ocho píxeles blancos de
margen interior; ese margen sobrevive a packAlternatives y a la repetición de
píxeles. La regresión verifica los cuatro bordes después de agrupar los tiles.
No se alteran los nombres aceptados, la confirmación temporal ni la regla de
nueve títulos antes del décimo retrato. Se conserva la prueba de 142 nombres.

Archivos modificados: DraftNameAtlas.kt, DraftNameAtlasTest.kt,
app/build.gradle.kts y este informe. El OCR instalado se valida en fix-error;
sus imágenes redimensionadas en emulador no son pruebas físicas de teléfonos.

Verificación local: 9/9 pruebas Python de auditoría aprobadas y diff limpio.
Las pruebas Kotlin locales se bloquearon por ausencia de Android SDK
(`SDK location not found`); quedan pendientes del workflow aislado.
