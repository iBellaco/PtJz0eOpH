# Coach 1.1.10.447 — build 1163

## Evidencia y causa

El propietario muestra dos capturas consecutivas con anillos aliados cada vez
más pequeños y bandas de texto desplazadas. El código pasaba la geometría ya
alineada como base al siguiente ajuste. Las alternativas de diámetro 0,8 y 1,2
se multiplicaban sobre esa estimación anterior, permitiendo encogimiento o
crecimiento acumulado; el ajuste de filas también reutilizaba su resultado.

## Corrección

- DraftVisionScanner mide siempre desde la calibración normalizada del viewport.
- DraftRingAlignment separa esa base de la geometría anterior. Usa la anterior
  para excluir los anillos realmente dibujados y conservar columnas sin evidencia.
  El diámetro se calcula desde la base, sin multiplicaciones acumulativas.
- DraftGeometryStability conserva cada columna ante ruido pequeño y exige dos
  mediciones consecutivas compatibles para cambiar una geometría ya establecida.
  La primera medición se admite directamente. Cambios de base, dimensiones o
  calibración reinician este estado. No se aumenta la frecuencia de captura.
- AGENTS.md y draft-recognition-contract.md recogen la regla multidispositivo:
  Android compatible con Wild Rift, dimensiones/viewport reales y evidencia
  visual, sin coordenadas por modelo ni afirmaciones de cobertura física universal.
- Se conserva el contenido de la última coach-validacion como base.

## Pruebas y límites

Se añaden pruebas de oscilación, ruido pequeño, confirmación independiente por
lado, reinicio de geometría y conservación sin evidencia. La regresión secuencial
ejecuta seis capturas sintéticas por tamaño (1280×587, 1280×590, 1600×720), cambia
el contenido de los retratos y dibuja el visor anterior sobre cada nueva imagen.
Comprueba puestos, filas y límites de diámetro a través de la secuencia.
Se incorpora DraftGeometryStabilityTest a la selección de pruebas de fix existente.

Correctos localmente: git diff --check, 10 pruebas de planes de validación,
15 de reutilización de APK y recursos localizados. El intento de las pruebas
Android no pudo arrancar al fallar la descarga de Gradle con Connection refused.
Las pruebas Android y la comprobación física de las capturas reportadas quedan
pendientes; no se entrega un APK anterior como si contuviera este arreglo.

Archivos modificados: AGENTS.md, docs/draft-recognition-contract.md,
app/build.gradle.kts, DraftVisionScanner.kt, DraftRingAlignment.kt,
DraftGeometryStability.kt, DraftGeometryStabilityTest.kt,
DraftPreviewAlignmentTest.kt, DraftRingAlignmentTest.kt y fix-error.yml.

## Aclaración documental posterior del propietario

Se incorpora a AGENTS.md y al contrato la secuencia de puestos: rivales estáticos
en el mismo viewport, aliados desplazables por fila según su estado, avatar con
título de línea como preselección y título completo de campeón como selección
sujeta a confirmación temporal. La corrección de escalado de esta versión no
equivale a implementar ese seguimiento individual; queda explícito para la
siguiente intervención. Solo se modifica documentación en esta aclaración.
La sincronización se aplazó mientras el fix estaba ejecutándose para evitar
cancelarlo mediante otro push. Se incorpora en 1.1.10.448 tras finalizar esa
ejecución correctamente.
