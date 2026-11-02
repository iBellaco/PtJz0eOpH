# Coach 1.1.10.456 — build 1172

## Diagnóstico

La ejecución 41462313386 ya superó el bloque de geometría/filas ocultas, pero
falló `formerlyFailingCentersStayAlignedWhenPortraitsChange` en paneles laterales
640 × 360 y 1024 × 768. La localización solo aceptaba aros cian aliados, mientras
la comprobación de visibilidad de retratos ya admitía los aros dorados de selección.
Descartar estos aros dejaba filas sin localizar o favorecía una escala menor
con coincidencias en los hechizos. La fila de baneos rival podía quedarse con
un candidato interior espurio y perder la posición exterior real: la extensión
de una fila de cinco candidatos estaba deshabilitada según la proporción.

## Cambios

- `DraftRingAlignment.kt`: admitir el color dorado de selección para aliados,
  conservando contraste interior, borde estrecho y cobertura angular exigidos.
- `BanDetection.kt`: permitir completar extremos con una comprobación explícita
  de aro, antes de seleccionar las cinco posiciones. No inventar identidades.
- `LiteRTVisionClassifier.kt`: proporcionar esa comprobación de aro; conservar
  exclusiones y comprobación de textura de los candidatos.
- `BannedPortraitScannerTest.kt`: cubrir fila panorámica con candidato interior
  espurio, posición exterior visible y rechazo de extensión sin aro.
- `app/build.gradle.kts`: versión 1.1.10.456, build 1172.

## Comprobaciones realizadas

- 75 pruebas de scripts, recursos de idiomas y `git diff --check`: correctos.
- Se compiló el archivo productivo `BanDetection.kt` con Kotlin 2.2.21 y se
  comprobaron tres proporciones, aro exterior visible, ausencia de aro y
  candidato excluido: correcto.
- Se ejecutó el archivo productivo `DraftRingAlignment.kt` con adaptadores
  temporales de Bitmap/Rect basados en AWT y los valores base del modelo. En
  dos escenarios de paneles laterales derivados de `draft-ten-selected.jpg`,
  los diez centros aliados tienen errores entre 0.42 y 2.55 píxeles de referencia,
  inferiores a la tolerancia existente de 6. Es diagnóstico de escritorio;
  no sustituye Skia/Android, las pruebas de identidad ni una prueba física.

La ejecución Android completa del arreglo sigue pendiente. Se conserva la
matriz de regresión que detectó el fallo, con sus umbrales y casos originales.
