# Coach 1.1.10.574 (1290) · Posiciones del draft y carga del autoescáner

Un perfil medido de fábrica bloqueaba el ajuste de los puestos incluso cuando los anillos visibles se habían desplazado. Ahora sirve como punto de partida. La calibración explícita tiene prioridad. Una fila aliada con evidencia débil y alejada de la columna del grupo conserva su posición anterior. El ajuste usa los mismos límites del juego durante el recorte y el reescalado.

En la cabecera, la presencia de varios retratos rivales podía impedir la relectura de una fila aliada ya localizada, y viceversa. La recuperación consulta la evidencia de cada equipo por separado. Las filas manuales se leen directamente en sus círculos.

Durante la selección, el escáner espera hasta el noveno pick para buscar indicios de cierre. Después basta encontrar tres anillos para confirmar que el draft sigue visible. El refinamiento de baneos reutiliza memoria, y el perfil manual también respeta el intervalo de medición del área visible. El bucle entra en pausa con la pantalla apagada o cuando Android comunica que la ventana compartida está oculta. La captura se reanuda bajo demanda.

## Archivos

- Captura y ejecución en segundo plano en `ScreenCaptureManager.kt`, `FloatingOverlayContent.kt` y `DraftSlotLifecycle.kt`.
- Coordenadas y reconocimiento en `DraftVisionScanner.kt`, `DraftRingAlignment.kt`, `BannedPortraitScanner.kt` y `LiteRTVisionClassifier.kt`.
- Regresiones en `DraftReferenceFrameTest.kt`, `BannedPortraitScannerTest.kt` y `DraftSlotLifecycleTest.kt`. Versión en `app/build.gradle.kts`.

## Verificación física pendiente

La verificación local cubrió 48 casos distintos de geometría, visibilidad, ciclo de selección y baneos. Dos casos de alineación revelaron un desplazamiento durante el ajuste inicial. Tras corregirlo, pasaron los 15 casos de alineación y la comprobación del perfil medido sobre una captura. También pasaron seis pruebas de los informes del escáner. Se usaron capturas de partidas, entre ellas las de POCO y Red Magic. El entorno local carecía de teléfonos Android conectados.

Para aceptar un modelo se requiere una partida de Wild Rift con captura y superposición activas en ese teléfono. Revisar una secuencia que combine filas vacías, preselecciones y títulos confirmados, y otra con un baneo parcialmente tapado. Registrar por equipo el índice y la identidad antes y después de volver al juego.

Para medir rendimiento, comparar en el mismo teléfono la versión anterior y esta versión con los mismos ajustes gráficos, brillo, límite de FPS y temperatura inicial. Registrar tiempos de fotograma del juego y consumo durante un intervalo equivalente con el visor activo, seguido de ocultar y restaurar la captura. Las capturas grabadas permiten comprobar geometría e identidades. La medición de batería y FPS requiere ejecutar el juego.
