# Auditoría técnica de Coach 1.1.10.543

## Alcance

Esta versión corrige la regresión que solo aparecía en la validación completa
del escáner. La captura de referencia del juego tiene una relación de aspecto
propia y no debe recibir la transformación genérica basada en 16:9.

## Cambio aplicado

- `DraftVisionScanner.kt`
  - Identifica la familia de capturas nativas de referencia mediante su relación
    de aspecto.
  - Evita aplicar la transformación adaptativa de 16:9 a esa familia.
  - Mantiene la adaptación para formatos externos y la proyección al viewport
    observado para capturas con barras.
  - Evita que el segundo slot aliado conserve una X reducida cuando la
    alineación visual aún no ha confirmado ese aro.
- `app/build.gradle.kts`
  - Incrementa la versión a 1.1.10.543 y el código de versión a 1259.

## Regresión corregida

La validación completa fallaba en `generic-1280-579`: el segundo slot aliado
quedaba en `X = 0.0579` en lugar de la posición de la captura de referencia,
con un error aproximado de 17.5 píxeles y una identidad visual incorrecta.
La causa era volver a escalar una captura que ya estaba en la proporción nativa
del viewport del juego.

## Verificación

- `DraftMultiDeviceAuditTest.phoneResolutionMatrix`: BUILD SUCCESSFUL.
- La prueba reproduce las variaciones de fondo, retratos, baneos y resoluciones
  de la matriz afectada.
- Se mantienen los perfiles medidos compilados vacíos y el respaldo de
  coordenadas sin entradas.
