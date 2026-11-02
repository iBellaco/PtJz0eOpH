# Coach 1.1.10.450 — build 1166

La captura reportada de Honor Magic 6 Pro muestra filas aliadas en posiciones
distintas durante la preselección y baneos aliados incompletos. Se elimina la
mediana de columna para aliados en DraftRingAlignment: cada observación actualiza
solo su fila; sin evidencia conserva su posición y diámetro anteriores.
DraftGeometryStability confirma cada aliado por separado en dos observaciones.
Se mantiene el ajuste conjunto rival y la base del viewport, sin presets de marca.

LiteRTVisionClassifier reparte el presupuesto de candidatos de baneo entre ambos
lados (100 retratos y 20 vacíos por lado), sin aumentar el total ni cambiar los
umbrales de identidad. Esto evita que un lado agote la búsqueda del otro.

Se amplían DraftRingAlignmentTest y DraftGeometryStabilityTest con filas mixtas,
fila única visible, retorno a posición anterior y diferentes dimensiones.
Las imágenes de estas pruebas son sintéticas: no equivalen a verificación física
del Honor ni garantizan que todos los retratos de la captura sean reconocidos.
La captura adjunta no está disponible como archivo local de regresión.
Compilación y regresiones Android pendientes de la ejecución de fix-error.
Comprobaciones locales: 75 pruebas de scripts aprobadas, recursos localizados
completos y diff sin errores de formato. Las pruebas Gradle locales se bloquean
al descargar la distribución (Connection refused); no se presentan como aprobadas.
