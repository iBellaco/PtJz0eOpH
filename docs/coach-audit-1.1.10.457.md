# Coach 1.1.10.457 — build 1173

## Fallo revisado

La ejecución 41465416778 superó el bloque inicial de 257 pruebas, pero falló
en `formerlyFailingCentersStayAlignedWhenPortraitsChange` y
`smallCapturePreservesJhinWhenPortraitsAndBansChange`. No era un error de permisos
del ejecutor: eran discrepancias reales de posiciones e identidades del escáner.

- En capturas cuadradas, reducir toda la superficie antes de medir consumía
  resolución en las barras negras y ampliaba la búsqueda vertical respecto al
  juego. El buscador podía terminar sobre los hechizos aliados.
- La extrapolación del último baneo heredaba la altura del recorte del campeón
  anterior y exigía un único radio promedio. En imágenes pequeñas ese recorte
  no coincidía con el aro real; se perdía el quinto baneo y entraba un candidato
  interior espurio.
- La matriz local ampliada detectó además un caso de remuestreo en 1920 × 869:
  sumar cobertura angular favorecía cinco coincidencias débiles de tamaño
  incorrecto frente a cuatro aros mejor localizados. Se corrigió antes del envío.

## Cambios

- `DraftRingAlignment.kt`: medir primero el contenido observado, transformando
  posiciones, tamaños, máscara y geometría previa hacia ese espacio y regresando
  al espacio de captura. Liberar el recorte temporal. Comparar cobertura por aro
  observado, no la suma de coincidencias; una fila ausente no obliga a cambiar
  el tamaño de las demás. Sin ajustes por fabricante/modelo.
- `BanDetection.kt`: extrapolar con la altura mediana observada de la fila y
  reservar primero la búsqueda hacia el extremo exterior. Mantener el límite
  de candidatos y la elección de cinco posiciones regulares.
- `LiteRTVisionClassifier.kt`: comprobar el aro exterior a tamaños cercanos al
  radio estimado. Mantener textura, exclusiones, umbrales de identidad y margen
  entre candidatos; localizar un círculo no confirma un campeón.
- `DraftRingAlignmentTest.kt`: regresiones de captura cuadrada con barras y
  remuestreo que no debe agrandar la columna por una coincidencia débil extra.
- `BannedPortraitScannerTest.kt`: regresión de extrapolación que no debe heredar
  el desplazamiento vertical del recorte del último campeón.
- `app/build.gradle.kts`: versión y build incrementados.

## Comprobaciones locales

- 75 pruebas Python de `.github/scripts`: correctas.
- 5 pruebas adicionales de informes JUnit, cobertura solicitada y resumen de
  auditoría del escáner: correctas.
- Verificación de recursos de idiomas y `git diff --check`: correctos.
- Compilación Kotlin 2.2.21 de los archivos productivos de alineación, geometría,
  recorte, detección de baneos y comparación de retratos, con adaptadores
  temporales de escritorio para las APIs Android.
- Diagnóstico AWT de 18 escenarios derivados de `draft-ten-selected.jpg`:
  640 × 360 con paneles laterales; 854 × 480 ajustado; 1080 × 1080 con barras;
  1024 × 768 con paneles; 1024 × 768 ajustado; 1920 × 869 ajustado. Cada uno con
  original, fondo cambiado y retratos/baneos permutados. Todos conservaron los
  diez retratos esperados con los umbrales productivos y las diez identidades
  de baneo; los centros de los recortes quedaron dentro de la tolerancia
  existente de seis píxeles de referencia (máximo observado: 2,86).
- 32 escenarios AWT sintéticos: filas aliadas con estados horizontales mixtos,
  filas ocultas con geometría previa distinta, y escalas de HUD 0,8/1,2 en varias
  dimensiones. Comprobaciones correctas, incluida conservación de filas ocultas.

Estos diagnósticos usan la lógica productiva y recursos locales, pero AWT no es
Skia/Android. No sustituyen las pruebas Robolectric nativas ni certifican ningún
teléfono físico. Las pruebas Android, firma, ofuscación e instalación siguen
pendientes de Actions; no se entrega un APK nuevo ni se promueve a validación
hasta disponer del resultado requerido. No se eliminó ni relajó ninguna de las
pruebas Android que detectaron los fallos.
