# Coach 1.1.10.551 — Validación de fuentes regionales

Versión: 1.1.10.551; versionCode: 1267.

## Cambios

- El validador de parche distingue las dos fuentes Global de la fuente
  regional China. La presencia de China ya no se interpreta como una tercera
  fuente global ni provoca un falso fallo de validación.
- Se mantiene la lectura del dataset JSON completo de WildRiftCore y la
  separación de estadísticas incompletas sin inventar porcentajes.

## Verificaciones

- Se añadió una regresión que acepta una fuente China adicional sin relajar la
  exigencia de `GamePatch.VERSION` para WildRiftFire y WildRiftCore.
- `python3 -m unittest discover -s .github/scripts -p test_patch_content.py`:
  correcto.
- `./gradlew :app:compileDebugKotlin --offline`: correcto.
- `./gradlew :app:testDebugUnitTest --tests com.example.GlobalTierScrapingTest`:
  correcto con JDK 21.

## Texto para pruebas

Coach 1.1.10.551

- La selección Global continúa usando sus dos fuentes y China permanece como
  región independiente.
- La validación ya no marca error por la fuente regional adicional.
- WildRiftCore recupera las tasas publicadas desde su dataset completo y no se
  muestran campeones con métricas incompletas como si fueran estadísticas
  válidas.
