# Continuidad: geometría multidispositivo del draft

Este documento describe el estado vigente del escáner de Coach. El catálogo de
mediciones aportadas por dispositivos fue eliminado para probar el motor
multidispositivo sin coordenadas precargadas.

## Estado vigente

- `MeasuredDeviceCalibrationDefaults` no contiene perfiles de equipos.
- `docs/scanner-calibration-backup.json` conserva únicamente un arreglo vacío.
- La aplicación comienza con la geometría genérica y la adapta al fotograma
  capturado, al viewport visible y a los aros detectados en tiempo de ejecución.
- Una calibración creada manualmente se guarda solo cuando el usuario la
  solicita; no se comparte con otro modelo ni se convierte en preset del
  proyecto.

## Reglas de la geometría adaptativa

- Trabajar con ratios normalizados y convertirlos al ancho y alto del fotograma
  que realmente recibe el lector.
- Detectar y excluir barras exteriores, recortes y áreas de navegación antes de
  alinear las regiones.
- Aplicar el ajuste de relación de aspecto únicamente al fotograma completo.
  Si existen barras y se observa un viewport interno, mapear la geometría base a
  ese viewport sin volver a escalarla por la relación de aspecto exterior.
- Alinear las columnas con los aros visibles mediante `DraftRingAlignment` y
  conservar una fila no observada hasta que exista evidencia suficiente.
- Tratar slots, baneos y cabecera superior como regiones independientes.
- Publicar exactamente la misma configuración efectiva al lector, al visor y a
  los diagnósticos.

## Archivos de implementación

- `VisionCalibrationConfig.kt`: ratios genéricos y persistencia explícita del
  usuario.
- `AdaptiveScreenLayoutEngine.kt`: adaptación por formato.
- `DraftFrameGeometry.kt`: límites del viewport y mapeo de barras.
- `DraftRingAlignment.kt`: verificación visual de las columnas.
- `DraftVisionScanner.kt`: selección de configuración efectiva.
- `MeasuredDeviceCalibrationDefaults.kt`: catálogo medido intencionalmente
  vacío.
- `docs/scanner-calibration-backup.json`: respaldo intencionalmente vacío.

Las imágenes y fixtures de pruebas son evidencia de regresión; no son perfiles
de producción ni se cargan como coordenadas por defecto.

## Criterio de aceptación

La corrección debe conservar la detección de slots y baneos al reescalar el
mismo fotograma en formatos panorámicos, estándar y casi cuadrados. No se
considera terminada si una dimensión externa o un perfil antiguo modifica la
geometría antes de que el escáner observe el viewport real.
