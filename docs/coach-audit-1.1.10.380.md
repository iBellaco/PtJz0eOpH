# Coach 1.1.10.380 — build 1096

La ejecución 40254924924 aprobó las pruebas unitarias, pero omitió VI en
draft-mel-nine.jpg a 1920×822, 1920×924 y 1920×889. Por ello bloqueó el
décimo Teemo. El margen blanco anterior no resolvió el reconocimiento.

La recuperación incorpora un tamaño de banda de 32 píxeles (16 de contenido
más márgenes) y una alternativa con los colores originales del título, sin
binarización. Ambas se agrupan en las solicitudes OCR existentes. El muestreo
usa la variante intacta para evitar que el borrado previo de maestría destruya
trazos tenues. No se incorporan identidades ni se relajan las coincidencias.

Ante otro fallo, la prueba instalada conserva los recortes cortos exactos de
las capturas reales en los artefactos de evidencia. Se mantiene la cobertura
obligatoria de 142 nombres y la secuencia nueve por texto/décimo visual.

Archivos: DraftNameAtlas.kt, DraftVisionScanner.kt, DraftNameAtlasTest.kt,
DraftSequenceMultiDeviceInstalledTest.kt, app/build.gradle.kts y este informe.
Pruebas locales: auditoría Python 9/9 y diff limpio. Las pruebas Android se
ejecutan en Actions porque el entorno local carece de SDK. La aprobación OCR
está pendiente; la evidencia será de emulador e imágenes, no de teléfonos físicos.
