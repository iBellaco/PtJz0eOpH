# Auditoría Coach 1.1.10.247

- Versión: 1.1.10.247
- Build: 963
- Problema: el último avatar reconocido podía quedar fuera del HUD aliado cuando faltaba la lectura OCR de su línea.
- Cambio: la confirmación final usa el hueco aliado único si no existe una línea reconocida. Se mantienen las salvaguardas que exigen nueve picks existentes, evitan duplicados y respetan huecos bloqueados.
- Escáner: conserva la selección visual confirmada antes de cerrar y desactiva el escaneo al desaparecer los espacios, con tolerancia a un fotograma perdido.
- Archivos modificados: `app/build.gradle.kts`, `app/src/main/java/com/example/service/ConfirmedLastPickHud.kt`, `app/src/test/java/com/example/TenthPickRegressionTest.kt`.
- Pruebas: se añadió regresión para el décimo pick aliado sin OCR de línea. La ejecución local de Gradle no inició porque el wrapper no pudo descargar Gradle 9.3.1 (conexión rechazada); ejecutar CI para compilar y correr las pruebas.
