# Continuidad: coordenadas del draft y soporte multidispositivo

Este documento es la referencia inicial para cualquier conversación nueva que modifique el escáner del HUD de Coach. Debe leerse junto con `AGENTS.md` y la auditoría más reciente.

## Regla obligatoria

El escáner debe funcionar con la geometría real del fotograma capturado en cualquier teléfono, resolución, relación de aspecto, recorte, barra del sistema o letterbox. Los perfiles medidos son puntos de partida y regresiones, no una lista cerrada de equipos compatibles.

- Guardar y calcular posiciones como ratios normalizados entre `0.0` y `1.0`.
- Aplicar los ratios al ancho y alto del fotograma que realmente recibe el lector.
- No aplicar directamente píxeles de la pantalla física a una captura reducida.
- Convertir las máscaras del HUD, textos de diagnóstico y overlay desde el espacio físico al fotograma con `CaptureCoordinateSpace.map`.
- Mantener ajustes manuales por dispositivo. Una copia restaurada en otro modelo no debe reutilizar la calibración geométrica del teléfono anterior.
- Alinear las columnas con los aros cian y rojos visibles mediante `DraftRingAlignment`; si una columna está oculta, conservar su perfil o calibración en vez de inventar otra posición.
- Los cinco slots, los baneos y la cabecera del décimo pick son regiones independientes.
- Toda modificación debe probar 20:9, 16:9, 4:3 y una captura panorámica, además de las capturas reales conservadas.

## Red Magic 10 Pro

Capturas de regresión del usuario: 1280×579/576 y captura de MediaProjection de 1920×868. El espacio físico observado es 2688×1216.

Perfil normalizado de referencia (`VisionCalibrationConfig`):

- Columna aliada: `X = 0.072`.
- Columna rival: `X = 0.959`.
- Y aliados: `0.188, 0.324, 0.458, 0.591, 0.725`.
- Y rivales: `0.188, 0.324, 0.457, 0.590, 0.722`.
- Diámetros aliados: `0.110, 0.110, 0.110, 0.110, 0.105` respecto al alto.
- Diámetros rivales: `0.110` en los cinco slots.
- OCR aliado X: `0.050..0.280`.
- OCR rival X: `0.805..0.925`.
- Fixtures: `bans/live-7448.png`, `bans/live-7449.png`, `bans/red-magic-header.jpg` y los JSON `live-red-magic-*.json`.

Conversión ya reproducida: una máscara publicada en 2688×1216 se proyecta a 1920×868 antes de excluir píxeles. Esta conversión evita tapar los nombres aliados y la cabecera rival.

## Poco X7 Pro

Identificadores conocidos: nombre comercial `Poco X7 Pro`, global `2412DPC0AG` y variantes cuyo modelo contiene `2412DPC0A` o `2412DPC0I`.

Coordenadas medidas proporcionadas por el tester:

- Columna aliada: `X = 0.120` en los cinco slots.
- Columna rival: `X = 0.911` en los cinco slots.
- Y aliados: `0.184, 0.320, 0.454, 0.592, 0.728`.
- Y rivales: `0.179, 0.315, 0.449, 0.587, 0.723`.
- Diámetro aliado: `0.110` en los cinco slots.
- El resto de diámetros, baneos, OCR y cabecera parte del perfil general y se ajusta con la imagen observada.

Captura real: 1280×576/579. MediaProjection observada: pantalla física 2712×1220 reducida a fotograma 1920×862.

Fixtures: `bans/poco-7457.png`, `bans/poco-header.jpg`, `draft/poco-7457.json` y `draft/poco-8067-header.json`.

El preset se aplica solo si no existen ajustes manuales del usuario. Restablecer debe recuperar este preset en un Poco reconocido. Los baneos manuales existentes se conservan durante la migración.

## S26 Pro del tester

Captura conservada: `app/src/test/resources/s26/draft.jpg`, 1280×591.

La columna rival visible se midió cerca de `X = 1168 / 1280 = 0.9125` (tolerancia de regresión `±0.012`). La columna aliada estaba tapada por el HUD y no existe una medición confiable; no se debe fabricar una coordenada estática para ella.

La solución para este equipo y modelos desconocidos es la alineación visual en tiempo de ejecución:

1. Excluir el rectángulo opaco del HUD y los círculos de depuración.
2. Buscar varios aros reales con contraste interior en cada columna.
3. Reemplazar solo la columna confirmada por varios slots.
4. Conservar el perfil vigente para una columna oculta.
5. Usar la configuración efectiva alineada tanto en el lector como en el visor.

La prueba `DraftRingAlignmentTest.s26VisibleRivalRingsReplaceAnotherPhonesCoordinates` impide volver a usar la X de otro teléfono sobre esta captura.

## Cabecera, baneos y décimo pick

Valores generales normalizados actuales:

- Baneos aliados X: `0.028, 0.064, 0.100, 0.136, 0.172`.
- Baneos rivales X: `0.826, 0.863, 0.900, 0.937, 0.974`.
- Y de ambas filas de baneos: `0.052`.
- Diámetro de ban: `0.060` respecto al alto.
- Cabecera Y: `0.045`; diámetro: `0.072`.
- Cabecera aliada X base: `0.028, 0.063, 0.098, 0.133, 0.168`.
- Cabecera rival X base: `0.834, 0.869, 0.904, 0.939, 0.974`.

`AdaptiveScreenLayoutEngine.computeAdaptiveConfig` vuelve a espaciar la cabecera según la relación de aspecto. La detección del décimo pick debe usar la coordenada individual del quinto avatar del lado pendiente, no la X global de la columna vertical.

## Archivos que no deben omitirse al modificar geometría

- `VisionCalibrationConfig.kt`: persistencia, presets y ratios.
- `AdaptiveScreenLayoutEngine.kt`: recortes y adaptación por formato.
- `CaptureCoordinateSpace.kt`: pantalla física a fotograma.
- `DraftRingAlignment.kt`: alineación por aros visibles.
- `DraftVisionScanner.kt`: configuración efectiva usada por el lector.
- `ScannerDebugOverlay.kt`: debe dibujar exactamente la misma geometría efectiva.
- `DeviceDraftProfileTest.kt`, `CaptureCoordinateSpaceTest.kt`, `DraftRingAlignmentTest.kt`, `TenthPickRegressionTest.kt`, `BannedPortraitScannerTest.kt` y las pruebas instaladas de draft.

## Criterio de aceptación multidispositivo

Una corrección no está terminada por funcionar solo en Red Magic o Poco. Debe conservar las detecciones conocidas de ambos, alinear la captura S26 y mantener resultados válidos al reescalar el mismo fotograma. Los círculos del visor deben coincidir con los recortes reales; los nombres de slots 1 y 2 no deben solaparse; los baneos no deben incluir retratos de perfil o fondo; y el décimo pick debe detener el escáner únicamente al confirmar la décima identidad.
