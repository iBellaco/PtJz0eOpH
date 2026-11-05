# Coach 1.1.10.479 — build 1195

## Geometría y lectura rápida del draft

- `DraftFrameGeometry`: convierte las medidas de referencia de 1280 × 579 a
  distancias desde los bordes escaladas por altura del viewport. Aplica los
  insets observados antes de publicar la geometría común al reconocimiento y visor.
  Las calibraciones manuales siguen siendo relativas a su captura.
- Muestrea tres filas para buscar franjas laterales casi negras. Conserva la
  detección de letterboxing vertical y la invalidación/caché existente (2 s,
  dimensiones, viewport y recalibración).
- `AdaptiveScreenLayoutEngine`: mantiene el recorte de avatar cuadrado incluso
  cuando la captura es menor que el diámetro mínimo de trabajo.
- `DraftNameAtlas` y `DraftVisionScanner`: la pasada rápida copia dos columnas
  directamente a un único bitmap, sin microbitmaps por título. Asigna únicamente
  las líneas dentro de las bandas calibradas; mantiene la recuperación de líneas
  aliadas y la pasada completa periódica. No procesa realces por píxel en la pasada rápida.
- Se conservan nueve títulos confirmados en fotogramas distintos como requisito
  para confirmar el décimo visual, el bloqueo de geometría rival y el cierre al
  abrir la build. Un avatar preseleccionado no cuenta como selección confirmada.
- Captura ya limitada proporcionalmente a 1920 × 1080: no se cambió su política.

## Alcance pendiente del pedido

No se ha convertido el reconocimiento de baneos a OCR exclusivo: las referencias
no garantizan texto de campeón en las diez posiciones. Se mantiene el comparador
de retratos y sus exclusiones. Tampoco se amplió la confirmación visual a los
primeros nueve puestos. La lectura por columnas es la vía rápida; las recuperaciones
periódicas completas siguen pudiendo superar dos solicitudes.

## Pruebas y límites

- `DraftMultideviceBoxesTest`: geometría sintética en 16:9, 19.5:9, 20:9,
  21:9, 16:10 y 4:3, insets asimétricos, regiones cuadradas, máscaras y píxeles
  sin deformación. Incluida en las regresiones obligatorias del arreglo.
- `DraftRivalNameInstalledTest`: nueve títulos y un jugador sin título en seis
  superficies sintéticas; rechaza el nombre de invocador ANNIE y registra mediana
  y p95 del OCR instalado. La prueba no certifica dispositivos físicos.
- Intento local de Gradle bloqueado por ausencia del SDK Android. Ninguna prueba
  Android local llegó a ejecutarse. Resultados de emulador pendientes.
- Menos de 150 ms es un objetivo pendiente de medición, no un resultado obtenido.
- No se dispone de celulares/tabletas físicos en este entorno. La evidencia
  sintética no acredita juego y visor funcionando juntos en cada dispositivo.
- Versión preparada para pruebas; no publicar como producción hasta validación.
