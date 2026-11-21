# Coach 1.1.10.582 (1298) · Línea aliada durante la selección

En la escena del Xiaomi 2412DPC0AG, la captura de 1280 × 576 muestra cuatro nombres aliados y «Carril central» en el quinto puesto vacío. El panel colocaba los nombres pendientes de línea en las primeras filas libres. El informe de coordenadas también imprimía una lista fija de líneas según la posición vertical.

El panel usa la línea leída y recordada para cada puesto. Si se conocen cuatro líneas distintas, el quinto puesto tiene la única línea restante. Las identidades pendientes siguen visibles en la lista de aliados sin línea y cuentan para el seguimiento del décimo pick. El informe de coordenadas incluye las líneas aliadas del mismo estado que los retratos. Las posiciones rivales se numeran.

La regresión de TenthPickRegressionTest reproduce a Sona, Mordekaiser, Zed y Kai’Sa con Mid reservado en el quinto puesto. Cubre la asignación del décimo pick, la recuperación de las líneas y las etiquetas del informe. La prueba usa los datos del caso. Pasaron 71 pruebas locales distintas de lectura de nombres, asociación de líneas, décima selección, cierre de sesión, informes y controles. La revisión de textos en portugués pasó. La verificación física en este dispositivo queda pendiente.

Archivos afectados: ConfirmedLastPickHud.kt, TenthPickHudPolicy.kt, OverlayState.kt, FloatingOverlayContent.kt, DraftVisionScanner.kt, VisionCalibrationConfig.kt, CalibrationControls.kt, DraftCalibrationPanel.kt, TenthPickRegressionTest.kt, DraftRivalNameInstalledTest.kt y app/build.gradle.kts.
