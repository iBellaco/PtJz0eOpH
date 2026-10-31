# Auditoría Coach 1.1.10.300

Versión 1.1.10.300, build 1016.

## Evidencia y alcance

El video aportado dura aproximadamente 0.51 segundos. Muestra el HUD fuera del borde derecho y el estado «Sin permiso». No permite determinar si el usuario rechazó el consentimiento, el sistema lo revocó o falló el inicio. No se dispone físicamente del S26 Pro; queda pendiente confirmar el comportamiento en ese equipo.

## Cambios

- `FloatingAssistantService.kt`: el arrastre descuenta el ancho del HUD; se corrige su posición tras medir el panel y se toma la orientación inicial real.
- `FloatingOverlayContent.kt`: tamaño máximo del HUD limitado al espacio disponible para distintas densidades y resoluciones.
- `MainDraftingScreen.kt`: Android 14 y posteriores solicitan compartir la pantalla completa, necesario para analizar el juego al salir de Coach; Android conserva la decisión de consentimiento.
- `ScreenCaptureManager.kt`: dimensiones máximas de pantalla, escalado uniforme sin mínimos que deformen la proporción, limpieza de sesiones fallidas y protección frente a callbacks tardíos de sesiones anteriores.
- `CaptureFrameDecoder.kt`: lectura por filas RGBA sin exigir relleno después de la última fila; el búfer original conserva su posición.
- `CaptureFrameDecoderTest.kt`: regresión para relleno de filas, búfer truncado y proporciones de varios dispositivos, incluidos pantallas anchas y plegables.
- `app/build.gradle.kts`: incremento de versión.

## Verificación

- `git diff --check`.
- Pruebas de captura específicas y verificaciones completas de Actions requeridas antes de publicar.
- Comprobar con testers: conceder captura completa, abrir Wild Rift, mover/expandir el HUD junto a ambos bordes, girar el dispositivo y renovar el permiso tras detener la captura desde Android.
