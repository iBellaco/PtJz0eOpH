# Coach 1.1.10.403 — build 1119

## Fallo comprobado

Ejecución 40607138941, revisión 80d80d30: los trabajos titulos-cortos
(122549385713) y espanol-instalado (122549385641) fallaron por la misma
aserción de lateSennaUsesShiftedEmptyLanesAndInfersFirstPickWithoutBanner:
`Unknown order must not label the allied tenth slot expected null, but was:<true>`.

El registro de titulos-cortos confirma que la secuencia sintética 1280x579
pasó antes del fallo. Al comenzar la siguiente medida, resetSlotMemory limpiaba
observedFirstPick pero no isFirstPickState. El visor conservaba el orden publicado
por la selección anterior. No es un fallo de compilación ni de cuenta de pruebas.
La suite instalada ejecutó 15 métodos y falló uno; los otros cuatro trabajos
de esta ejecución terminaron correctamente. Esto no aprueba la nueva revisión.

## Corrección y archivos

- DraftVisionScanner.kt: el reinicio común borra también el orden publicado,
  incluso con preserveBans=true. clearSessionState delega en ese mismo reinicio.
- DraftRivalNameInstalledTest.kt: regresión explícita para ambos órdenes previos,
  ambos modos de conservación de baneos, reinicio repetido y cierre de sesión.
  Se conserva sin debilitar la prueba Senna de dos dimensiones y sus aserciones.
- tools/verify-targeted-red-magic.py: ejecuta la nueva regresión antes del OCR
  dirigido. La suite completa descubre ahora 16 métodos desde el código fuente.
- app/build.gradle.kts: incremento de versión; este documento registra la auditoría.

## Verificación y límites

Las regresiones Python de los verificadores y el chequeo de diferencias se ejecutan
localmente antes del envío. Las pruebas Android se delegan a fix-error porque
el entorno local no tiene SDK Android configurado. No se declara aprobada esta
corrección hasta obtener resultados de esa revisión en Actions.

Las pruebas instaladas son de emulador; las secuencias de Senna son sintéticas.
No son pruebas físicas de Red Magic, POCO ni de ningún otro teléfono.
No se cambian main, coach-validacion, firma productiva ni umbrales de reconocimiento.
