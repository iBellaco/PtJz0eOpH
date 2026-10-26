# Coach 1.1.10.382 — build 1098

La ejecución 40261636488 aprobó las pruebas unitarias pero volvió a omitir VI
en los tres escenarios release. ML Kit devolvió NI en 1920×924. No se acepta
esa lectura como alias de VI ni se completa el noveno nombre con el retrato.

La imagen de diagnóstico sequence-49-enemy-first-draft-mel-nine.jpg-short-4.png
ya excluye el fondo distante, pero muestra un resto del emblema pegado a V.
La alternativa medía los límites de tinta desde los píxeles originales, lo
que reintroducía ese resto pese a haber calculado antes el margen del emblema.
Ahora mide los límites sobre la alternativa con el emblema suprimido; la variante
de color sigue tomando los píxeles originales dentro de esos mismos límites.

Archivos: DraftNameAtlas.kt, app/build.gradle.kts y este informe. La regresión
existente conserva los tres escenarios y la cobertura obligatoria de 142 nombres.
Verificación local: auditoría Python 9/9 y diff limpio. La validación Android
queda pendiente de Actions: no hay SDK local. Las pruebas instaladas sobre
imágenes en emulador no equivalen a pruebas físicas de teléfonos.
