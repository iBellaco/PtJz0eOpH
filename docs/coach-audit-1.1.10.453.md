# Coach 1.1.10.453 — build 1169

## Error reproducido

La ejecución de 1.1.10.452 falló en
`mixedAlliedRowsAndOneVisibleRowKeepTheirOwnCentersAcrossDimensions`:
la fila 3 esperaba X normalizada 0.125 y recibió 0.0890625 en 1280 × 585.
La mediana de tres filas desplazaba las otras dos pese a sus anillos visibles.

## Modificación

- `DraftRingAlignment.kt`: usar el ajuste común solo para observaciones débiles;
  conservar X/Y independientes con al menos 32 de 40 muestras de contorno.
  Las filas no observadas conservan su geometría anterior y su diámetro.
- `DraftRingAlignmentTest.kt`: ampliar los casos mixtos con una sola fila
  desplazada frente a tres alineadas y con filas ocultas junto a dos visibles.
- `fix-error.yml`: incluir el caso concreto de centros que falló en la
  validación anterior para detectarlo antes de promover el arreglo.
- `app/build.gradle.kts`: incrementar versión y build.

## Verificación

Pasan 75 pruebas de scripts, la comprobación de recursos españoles/portugueses
y `git diff --check`.

Las pruebas Android requieren la ejecución remota: el entorno local no puede
descargar Gradle (Connection refused). No se afirma que pasen ni se declara
verificación física multidispositivo. Se mantienen las tolerancias existentes.
