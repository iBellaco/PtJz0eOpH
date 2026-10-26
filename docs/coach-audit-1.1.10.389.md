# Coach 1.1.10.389 — build 1105

La ejecución 40307129897 aprobó el job debug completo y falló en los tres casos
release por VI ausente en draft-mel-nine.jpg. El artefacto raw-repeated-4.png
conserva VI visible en color original, pero ML Kit no devuelve texto para ese
recorte aislado. La binarización tampoco produce una lectura exacta aceptable.

Se añade una recuperación con contexto de texto realmente observado en la misma
captura: a ambos lados del título pendiente se copian los píxeles de otro título
que el OCR ya leyó. No se dibujan nombres ni se inyecta el valor esperado. Solo
se acepta texto cuyos elementos OCR estén completamente dentro del rectángulo
del título pendiente. Los nombres de contexto no se asignan al slot objetivo.
Se mantienen la comparación completa con catálogo y las confirmaciones temporales.
La recuperación se ejecuta únicamente si los intentos previos no resolvieron el
título; el atlas contextual no se reagrupa perdiendo su contexto.

La regresión verifica que las regiones de contexto queden fuera del slot y que
la fuente siga perteneciendo al título original. La instrumentación guarda
context-*.png, texto, rectángulo objetivo y cajas de cada elemento para evaluar
una lectura real, no una inferencia a partir del resultado esperado.

Archivos: DraftNameAtlas.kt, DraftVisionScanner.kt, DraftNameAtlasTest.kt,
DraftSequenceMultiDeviceInstalledTest.kt, app/build.gradle.kts y este informe.
Verificación local: diff limpio y 9 pruebas Python. No se equiparan esas pruebas
con aprobación del escáner: Kotlin/ML Kit quedan pendientes de Actions por falta
de SDK/emulador local. Se mantienen los 142 nombres, cinco casos aislados y
Red Magic. Toda evidencia de imágenes/emulador se separa de pruebas físicas.
