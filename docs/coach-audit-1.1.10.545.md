# Auditoría de Coach 1.1.10.545

## Corrección final

- Se mantiene el monitor global con solo WildRiftFire y WildRiftCore.
- La vista China / Local conserva sus categorías T0–T5 sin presentar tasas
  globales como si fueran estadísticas regionales.
- Una recarga forzada del catálogo vuelve primero a Global y después permite
  restaurar la preferencia regional. Esto evita que una actualización global
  quede oculta por una selección China anterior en otra sesión o prueba.
- La pantalla China / Local muestra únicamente campeones con clasificación
  local; las entradas sin esa clasificación no aparecen.

## Validación

- Compilación Kotlin y Java de `testDebugUnitTest`: correcta con el SDK y el
  JDK configurados del entorno.
- Suites dirigidas de scraping, monitorización de fuentes, persistencia de
  región, parser de metadatos y catálogo: 41 pruebas correctas.
- Validadores de contenido de parche y recursos localizados: correctos.
- `git diff --check`: correcto.
- La suite completa de pruebas contiene una matriz Robolectric de detección
  multidispositivo de larga duración; se detuvo después de comprobar la
  compilación y se ejecutaron las suites afectadas de forma dirigida.
