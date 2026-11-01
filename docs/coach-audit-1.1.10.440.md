# Coach 1.1.10.440 — build 1156

- Captura bajo demanda: desconectar la superficie después de 1,5 segundos sin lecturas; reanudar el mismo VirtualDisplay sin reutilizar el consentimiento ni recrear la proyección. Pantalla apagada suspende la producción.
- Descartar fotogramas anteriores al reanudar. Escaneo manual espera acotada por un fotograma nuevo y libera siempre el bitmap y el indicador de trabajo.
- Arrastre: cancelar la animación de borde antes de un nuevo gesto, medir el destino real y distinguir soltar de cancelar. Ocultar ventanas al aceptar cierre y retirarlas antes de limpiar datos.
- No se alteran las confirmaciones de identidad ni la secuencia de selección.

Prueba añadida a FloatingWindowLifecycleTest: suspensión, reanudación, pantalla apagada y liberación de captura. Compilación y prueba Android pendientes de fix. Consumo y fluidez requieren medición física comparativa; no se garantiza un porcentaje de ahorro.

Archivos: ScreenCaptureManager.kt, FloatingAssistantService.kt, FloatingOverlayContent.kt, FloatingWindowLifecycleTest.kt, app/build.gradle.kts.

Comprobaciones locales: recursos localizados completos y diff sin errores de formato. La prueba Android no pudo iniciarse: descarga de Gradle rechazada por la conexión del entorno. Se incluye en la comprobación existente de FloatingWindowLifecycleTest en fix, sin añadir ejecuciones paralelas nuevas.
