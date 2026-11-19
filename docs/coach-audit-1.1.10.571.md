# Coach 1.1.10.571 (1287) · Escáner en distintas proporciones y baneos parcialmente visibles

## Cambios

- Los puestos del draft se proyectan una sola vez desde la referencia de 1280 × 579 al área visible. Antes, las capturas sin barras y alejadas de esa proporción recibían además una transformación desde 16:9. La distancia de las columnas a los bordes cambiaba según el ancho del dispositivo.
- Cada equipo conserva las coordenadas y el tamaño de sus baneos calibrados en la captura. La fila del otro equipo puede seguir adaptándose.
- Si la búsqueda general pierde la fila, se vuelven a leer los círculos calibrados o ya localizados en la misma ventana y tamaño de captura. Los recortes respetan las máscaras y los ajustes verticales manuales. Sigue siendo necesario reconocer al campeón en dos fotogramas.

## Archivos

- Escáner y geometría en `DraftVisionScanner.kt` y `DraftFrameGeometry.kt`.
- Lectura y seguimiento de baneos en `LiteRTVisionClassifier.kt` y `BannedPortraitScanner.kt`.
- Regresiones en `DraftMultideviceBoxesTest.kt` y `BannedPortraitScannerTest.kt`.
- Versión en `app/build.gradle.kts`.

## Comprobaciones

- Una reproducción local en JVM, con un adaptador de imágenes de escritorio, verificó la distancia al borde en 1280 × 720, 1600 × 1000 y 2520 × 1080 y la calibración independiente de ambos equipos con barras asimétricas.
- En la captura POCO 7457 se ocultaron todos los baneos salvo Hwei. La misma reproducción verificó que el puesto aislado quedó sin confirmar al iniciar sin referencia y que dos lecturas lo confirmaron al disponer de la fila localizada o calibrada.
- Pasaron 19 pruebas de las herramientas del escáner y seis de selección de pruebas. Se comprobaron el incremento de versión y los recursos de idioma.
- Quedan por confirmar la ejecución de las regresiones Android y el comportamiento en los teléfonos afectados.
