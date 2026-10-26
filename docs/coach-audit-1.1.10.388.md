# Coach 1.1.10.388 — build 1104

La ejecución 40302508420 aprobó el job completo debug (incluido ORNN, caso 42)
pero omitió VI en los tres casos release. La corrección anterior no está validada.

La limpieza recuperaba el emblema desde una banda ya ampliada a 150 píxeles.
Ahora el atlas conserva una referencia prestada a la captura durante el escaneo.
La recuperación copia la banda a resolución nativa, aplica las mismas exclusiones
y elimina el emblema antes de escalar. Así evita hacer la conectividad sobre
trazos previamente mezclados por la interpolación. La captura no se modifica ni
se recicla; una regresión compara los píxeles antes/después y comprueba su vida.
Las máscaras se copian para evitar cambios externos y se reaplican a la banda.

Archivos: DraftNameAtlas.kt, DraftNameAtlasTest.kt, app/build.gradle.kts y este
informe. Se mantienen las coincidencias completas, la confirmación temporal,
las pruebas de 142 nombres y todos los errores conocidos en fix-error.
Pruebas locales: Python 9/9 y diff limpio; Kotlin y ML Kit pendientes de Actions
por falta del SDK local. No se presentan como pruebas físicas las imágenes
instaladas en emulador. main y coach-validacion permanecen sin cambios.
