# Coach 1.1.10.378 — build 1094

La ejecución 40249503286 superó la corrección de sintaxis y Red Magic, pero
omitió el título VI en draft-mel-nine.jpg para las geometrías 1920×822 y
1920×889. Al faltar ese noveno título, Teemo quedó bloqueado correctamente.

La alternativa de títulos cortos escalaba la banda completa, conservando mucho
margen vacío. Ahora mide los límites de tinta de los mismos píxeles separados
por color y escala ese contenido. Conserva el mapeo espacial al título original,
la coincidencia de nombre completo y las confirmaciones temporales. No utiliza
retratos para completar los primeros nueve ni incorpora nombres esperados.

Archivos: DraftNameAtlas.kt, DraftNameAtlasTest.kt y app/build.gradle.kts.
La regresión de las tres dimensiones exige también que se reduzca el margen
vertical. La ejecución aislada conserva los tres escenarios release fallidos
y la cobertura de 142 nombres. El reconocimiento nativo queda sujeto a Actions;
son imágenes redimensionadas sobre emulador, no pruebas físicas de teléfonos.

Verificación local: 9/9 pruebas Python de auditoría secuencial aprobadas y
`git diff --check` limpio. Las pruebas Kotlin no pudieron ejecutarse: este
entorno carece de Android SDK (`SDK location not found`). Su ejecución y el
resultado OCR quedan pendientes del workflow aislado, sin declararse aprobados.
