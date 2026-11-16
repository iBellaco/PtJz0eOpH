# Coach 1.1.10.557 — corrección de localización de China y Elo

Versión: 1.1.10.557; versionCode: 1273.

## Corrección

- Se añadieron al catálogo portugués las frases del selector China por Elo y
  del detalle de segmentos que incorporó la versión anterior.
- El texto visible del selector ya no queda en español cuando la aplicación
  está configurada en portugués.

## Validación

- `bash tools/audit-portuguese.sh`: cero hallazgos de texto español visible.
- La compilación Kotlin y las pruebas dirigidas de parser China, selección de
  servidor y actualización de marcos ya habían terminado correctamente en la
  versión anterior.

## Texto para pruebas

Coach 1.1.10.557

- Cambia el idioma a portugués y abre la tier list China.
- Comprueba que el selector de segmentos Elo y su explicación aparecen en
  portugués.
- Confirma que el monitoreo conserva las filas Global y China.
