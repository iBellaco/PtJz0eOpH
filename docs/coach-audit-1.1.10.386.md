# Coach 1.1.10.386 — build 1102

La ejecución 40293645603 aprobó las pruebas unitarias (incluida la cobertura
de 142 nombres) y los casos release 1920×924 y 1920×889. Solo 1920×822 sigue
omitiendo VI en draft-mel-nine.jpg y bloqueando Teemo. La regresión del cuadro
blanco está corregida; no se declara aprobado el escáner completo.

La repetición de título anterior utilizaba exclusivamente píxeles binarizados
a 72 píxeles de alto. La alternativa nueva repite los mismos píxeles en color
original, conservando el suavizado y a 32 píxeles de alto con margen interior.
Se mantiene la selección de límites neutra y todas las variantes ya existentes.
No se crean letras, nombres ni alias; se añade una comprobación de igualdad de
todos los píxeles de las tres copias contra el recorte original. La instrumentación
conserva el recorte original-color-repeated y el resultado OCR si vuelve a fallar.

Archivos: DraftNameAtlas.kt, DraftVisionScanner.kt, DraftNameAtlasTest.kt,
DraftSequenceMultiDeviceInstalledTest.kt, app/build.gradle.kts y este informe.
Pruebas locales: auditoría Python 9/9 y diff limpio. Kotlin/ML Kit pendientes
de Actions por ausencia del SDK local. Evidencia de imágenes sobre emulador,
no pruebas físicas. main y coach-validacion permanecen sin cambios.
