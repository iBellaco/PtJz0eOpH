# Coach 1.1.10.458 — build 1174

## Fallo confirmado

La ejecución 41471186614 falló en la matriz de centros. Los baneos y el caso
de captura pequeña ya no presentaron el error anterior. Persistían dos escenarios
aliados: captura ajustada de 1920 × 864 y paneles laterales de 1024 × 768.

Esta vez se reprodujo el fallo localmente con Robolectric 4.16.1, API 34 y
gráficos NATIVE, usando los archivos productivos de geometría/calibración/recorte.
Antes de modificar el algoritmo se obtuvieron exactamente las desviaciones de
Actions: 6,131372 / 6,329206 / 8,60276 / 8,536821 píxeles en el primer escenario
y 63,000744 / 36,97922 / 62,974606 / 36,974113 en el segundo. AWT no había
reproducido estas diferencias de rasterización; no se utilizó como prueba final.

La selección de escala favorecía ganancias marginales en cobertura media:
24 frente a 23,8 muestras, o 25,67 frente a 25. Eso bastaba para reducir un 20%
el diámetro, descartar filas observadas y seleccionar detalles de los hechizos.

## Cambios

- `DraftRingAlignment.kt`: exigir cuatro muestras angulares adicionales por
  aro observado (10% de las 40 muestras) para sustituir la escala base por una
  alternativa. Conservar la comparación media, la búsqueda por dimensiones,
  los cambios de posición por fila y la geometría previa de filas ocultas.
  La ausencia de ajuste nominal sigue permitiendo recuperar otra escala.
  No se modificaron los umbrales de identidad de campeones ni baneos.
- `DraftRingAlignmentTest.kt`: añadir regresión nativa con ambos escenarios,
  verificando centros y diámetros. Se mantiene el límite de seis píxeles y
  las pruebas existentes de cambios legítimos de HUD 0,8/1,2.
- `app/build.gradle.kts`: versión 1.1.10.458, build 1174.

## Comprobaciones realizadas

- 15 pruebas de `DraftRingAlignmentTest`, compilando directamente los archivos
  productivos y ejecutando JUnit/Robolectric NATIVE API 34: correctas. Incluyen
  filas móviles, filas ocultas, máscaras, cambios de tamaño, barras negras y
  la nueva regresión.
- Matriz nativa aislada adicional de 21 escenarios: siete combinaciones de
  dimensiones/disposición, cada una con imagen original, fondo sustituido y
  retratos/baneos permutados. Geometría, recortes, ranking, confirmación visual
  en dos observaciones y diez identidades de baneo: correctos. Máximo error de
  centro: 5,086 píxeles de referencia, dentro del límite existente de seis.
- Se usaron Bitmap/Canvas reales de Robolectric NATIVE y el código productivo
  de comparación de retratos. El arnés aislado utiliza adaptadores temporales
  para el catálogo local, preferencias y registro; no equivale a ejecutar toda
  la aplicación. Las imágenes del catálogo son los recursos del proyecto.
- 75 pruebas Python de `.github/scripts` y cinco de informes/cobertura del
  escáner: correctas. Recursos de idiomas y `git diff --check`: correctos.

El entorno se preparó siguiendo la guía de ejecución y proxy de la sesión.
Las dependencias de diagnóstico quedaron fuera del proyecto; no se cambiaron
sus dependencias de compilación ni credenciales.

La comprobación completa de Actions y la entrega APK firmada/ofuscada quedan
pendientes del nuevo envío. No se afirma haber probado teléfonos físicos ni
la instalación de esta versión. No se promueve a validación hasta que el
arreglo termine correctamente; no se fusiona la rama principal.
