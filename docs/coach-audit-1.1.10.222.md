# Auditoría de Coach — 1.1.10.222 (build 938)

## Problema y cambios

- Abrir el visor desde cualquiera de los dos accesos forzaba Auto-Scan a activo. Se elimina ese cambio: consultar el visor respeta la captura activa o pausada y conserva los equipos y las selecciones manuales.
- Reanudar Auto-Scan llamaba a resetSlotMemory y descartaba los nombres, roles y evidencia del retrato. Reanudar conserva esas detecciones; Vaciar equipos mantiene el reinicio explícito para comenzar otro draft.
- El visor dejaba la espera del bucle en 50 ms incluso con diez campeones completos. Su apertura ya no modifica la cadencia de análisis ni reinicia la proyección.
- Los tres accesos de la cabecera y el interruptor de Auto-Scan tienen áreas de toque de 48 dp. Se agrega una etiqueta de prueba al interruptor.
- La segunda captura reportada de Vi reproduce una coincidencia de aproximadamente 79 % con las referencias anteriores, insuficiente para el umbral de 80 %. Las referencias tenían escalas separadas por 15 %; se agregan dos escalas intermedias con desplazamientos de 3 %. Las 45 referencias por campeón se calculan y guardan en caché, sin usar un servicio externo. Se conservan las 27 referencias anteriores.
- No se reduce el umbral: se mantiene 80 %, margen de 8 puntos frente al segundo candidato, dos fotogramas estables, nueve campeones previos y exclusión de campeones ya elegidos o bloqueados. Se respetan umbrales personalizados y bloqueos manuales del draft.
- Se conserva la identidad Coach, com.Coach, español/portugués y la firma persistente. La entrega se compila en release con R8.

## Verificación

Se incorporan únicamente los recortes de los retratos de Vi de las dos capturas como fixtures, sin nombres de jugadores. Se amplía PortraitMatcherTest con ambos recortes frente al catálogo completo, confirmación del décimo campeón y colocación en la vacante de jungla de ambos lados, preservación de las otras nueve selecciones y bloqueos manuales. Se prueban también umbral personalizado de 95 %, ocho selecciones previas y exclusión de Vi. Permanecen las regresiones de Volibear, icono de espera, ambigüedad y transición a carga.

LiteRTViewerNavigationTest ejercita los controles reales del asistente: ojo, abrir/cerrar visor y reanudar Auto-Scan. Comprueba conservación del estado pausado/activo, equipos, selección manual, memoria de nombres, roles y reporte. Se revisan español y portugués.

El análisis local de los píxeles sitúa ambas capturas por encima del 86 % con las referencias intermedias. Este documento se prepara antes de la ejecución de Gradle y de las comprobaciones del APK: la entrega incluirá el acta con los resultados efectivos. compile_applet no está disponible; se utiliza la compilación y los dispositivos de validación de GitHub Actions del repositorio.

## Archivos modificados

README.md, app/build.gradle.kts, FloatingOverlayContent.kt, PortraitMatcher.kt, PortraitMatcherTest.kt, LiteRTViewerNavigationTest.kt, portraits/vi-reported-draft.json y portraits/vi-reported-viewer.json.

## Resumen para testers

Coach 1.1.10.222: comprobar que abrir/cerrar el visor o tocar el ojo conserva el draft y el estado de Auto-Scan; que pausar y reanudar no borra detecciones; y que Vi se añade a la vacante de jungla después de nueve selecciones. Revisar español y portugués y confirmar que las selecciones manuales se respetan.

## Commit copiable

fix: conservar el escaneo al usar accesos y confirmar Vi en el draft de Coach
