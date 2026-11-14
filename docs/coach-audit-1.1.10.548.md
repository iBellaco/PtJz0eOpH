# Coach 1.1.10.548 — Fluidez al compartir recursos con Wild Rift

Versión: 1.1.10.548; versionCode: 1264.

## Problemas identificados y cambios

- Las recomendaciones del draft se calculaban dentro de `remember`, bloqueando
  el hilo de interfaz tanto en la aplicación como en el overlay. Ahora reciben
  entradas inmutables y se calculan en un trabajador compartido de concurrencia
  uno. Una solicitud cancelada no publica recomendaciones del draft anterior.
  Mientras se calcula se mantiene un resultado vacío, sin inventar sugerencias.
- El pulso Auto-Scan leía el estado de animación durante la composición del hub.
  Ahora las lecturas se realizan en dibujo/capas gráficas: el indicador conserva
  su animación sin recomponer el contenido completo en cada cuadro.
- Un análisis lento solo dejaba 30 ms antes del siguiente. Se introduce descanso
  de 180 ms como mínimo, proporcional al procesamiento (máximo 1200 ms para la
  recuperación), y al menos 600 ms con ahorro de energía o estado térmico moderado.
  Se usa reloj monotónico y se propaga correctamente la cancelación.
- Capturas con relleno nativo entre filas creaban un búfer directo grande por
  fotograma. Se reutiliza uno por sesión, sin compartir bitmaps entre consumidores.
  Se libera al cerrar la proyección o recibir presión de memoria.
- Se sustituyen recolecciones forzadas de memoria por la liberación de imágenes
  temporales y evidencia regenerable, manteniendo picks, baneos y calibración.
- El hilo auxiliar de captura tiene prioridad de segundo plano.
- La admisión de fotogramas de baneos pasa de 250 a 750 ms: reduce copias de
  pantalla y búsquedas en paralelo con OCR, conservando la cola del más reciente.

## Archivos

- `app/build.gradle.kts`
- `app/src/main/java/com/example/ui/components/RememberDraftAnalysis.kt`
- `app/src/main/java/com/example/ui/screens/MetaAndDraftScreen.kt`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/main/java/com/example/service/FloatingAssistantService.kt`
- `app/src/main/java/com/example/service/screen/CaptureFrameDecoder.kt`
- `app/src/main/java/com/example/service/screen/ScreenCaptureManager.kt`
- `app/src/main/java/com/example/service/screen/DraftSyncCadence.kt`
- `app/src/main/java/com/example/service/screen/DraftBanWorker.kt`
- `app/src/main/java/com/example/service/screen/DraftVisionScanner.kt`
- Pruebas: `CaptureFrameDecoderTest`, `CoachRoutineRegressionTest`,
  `DraftAnalysisSchedulingTest`, `DraftBanWorkerTest`.

## Verificación y límites

37 pruebas locales dirigidas correctas: reutilización de búfer sin sobrescribir
imágenes anteriores, pausa tras OCR lento, cancelación de recomendaciones,
procesamiento paralelo de baneos, calidad/texto de recomendaciones y pantalla
de selección. Comprobación final correcta tras hacer que el guardado automático
espere el análisis actualizado (`BUILD SUCCESSFUL`).

`tools/update-champion-builds.py --check`, `validate-patch-content.py`, las seis
pruebas de recursos localizados y `git diff --check`: correctos.

Se conservan resolución, proporciones, cajas OCR y confirmación temporal de
campeones. No se añaden perfiles de dispositivos ni coordenadas guardadas.
No se han medido todavía FPS físicos, RAM o consumo de batería antes/después con
Wild Rift en teléfonos. La menor frecuencia de escaneo bajo carga tiene como
contrapartida una espera adicional antes de detectar cambios. Se requiere
validación del APK ofuscado e instalado y comparación física antes de afirmar
que desaparecieron todos los bloqueos o cuantificar ahorro.

## Texto para pruebas

Coach 1.1.10.548

- Ajustado el cálculo de recomendaciones para mantener la interfaz disponible.
- Optimizada la animación del indicador Auto-Scan.
- Añadidos descansos del escáner según carga, temperatura y ahorro de energía.
- Reducida la creación repetida de memoria temporal durante las capturas.
- Pendiente comprobar fluidez, temperatura y batería con Wild Rift en equipos físicos.
