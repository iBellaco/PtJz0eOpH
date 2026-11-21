# Coach 1.1.10.579 (1295) · Alineación de baneos y selección tras guardar el perfil

En el Xiaomi 24122RKC7G, la fila aliada anterior situaba los primeros centros en
88 y 112 px para una captura de 1280×576 con retratos de 35 px de diámetro.
MeasuredDeviceCalibrationDefaults usa los extremos visibles para repartir los
cinco centros a la altura de la fila. Al cargar el perfil, reconoce los valores
anteriores exactos y respeta los ajustes individuales posteriores.

DraftVisionScanner y VisionCalibrationConfig comparan la geometría de los
puestos con la del modelo medido al reabrir una calibración. Guardar una fila
de baneos permite seguir ajustando los puestos a los anillos del juego.

DeviceDraftProfileTest y DraftReferenceFrameTest cubren la separación en
píxeles, la corrección del perfil anterior y la reapertura tras editar baneos.
Se añadieron ambos casos a fix-error.yml y se actualizó app/build.gradle.kts.
Pasaron las verificaciones locales de contenido, recursos y diferencias.
La descarga de Gradle fue rechazada en el entorno local. La ejecución Android
y la comprobación física en los equipos afectados quedan pendientes.
