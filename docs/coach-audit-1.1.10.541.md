# Auditoría técnica de Coach 1.1.10.541

## Alcance

Esta versión corrige la geometría de selección de campeones para relaciones de aspecto distintas de 16:9 y hace más resistente la detección visual de la fila superior de baneos.

## Cambios aplicados

- `AdaptiveScreenLayoutEngine.kt`
  - Escala la geometría genérica de slots laterales y sus bandas OCR desde la referencia 16:9 según la relación de aspecto.
  - Conserva sin alterar las calibraciones explícitas, manuales y las medidas guardadas por dispositivo.
  - Limita los resultados a los bordes físicos válidos del frame.
- `BanDetection.kt`
  - Calcula el ancho de las bandas laterales a partir de la relación de aspecto.
  - Añade regiones teóricas de cinco baneos con paso proporcional a la altura.
  - Descarta retratos adyacentes cuando una fila regular tiene evidencia del anillo exterior.
- `LiteRTVisionClassifier.kt`
  - Amplía las muestras verticales de la cabecera para absorber barras del sistema, gestos y recortes de cámara.
  - Mantiene las muestras históricas para no perder capturas ya validadas.
  - Conserva evidencia válida de un solo bando y exige al menos dos propuestas.
  - Filtra propuestas que no pertenecen al mismo clúster vertical de la fila.
  - Incluye el borde exterior en la búsqueda sin reintroducir la banda central.
- `BannedPortraitScanner.kt`
  - Asocia filas incompletas a regiones nominales por proximidad cuando hay posiciones dispersas.
  - Conserva coordenadas medidas cuando la fila es continua o completa.
  - Respeta calibraciones manuales y mantiene el quinto slot rival como vacío cuando corresponde.
- `DraftFrameGeometry.kt`
  - Tolera negro con tinte ligero y artefactos aislados de barras de gestos al encontrar los límites del contenido.

## Validación

Se añadieron regresiones para:

- Geometría genérica 16:9 y 20:9.
- Conservación de calibración explícita.
- Paso adaptativo de baneos.
- Asignación de observaciones incompletas por proximidad.
- Filas rescaladas y posiciones no uniformes.
- Barras casi negras con artefactos de navegación.

Las pruebas unitarias específicas de geometría, escáner de baneos, límites de contenido y regresiones de draft terminaron correctamente.
