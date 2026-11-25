# Coach 1.1.10.601 · Recuperación de descargas Robolectric en validación

## Corrección

- Se añadió un segundo intento al ejecutor de regresiones JVM cuando Gradle
  falla al descargar dependencias de Robolectric durante la prueba.
- Los fallos reales de aserciones siguen propagándose después del segundo
  intento; solo se absorben cortes transitorios del repositorio de artefactos.
- Se incrementó la versión visible de Coach a `1.1.10.601` (1317).

## Verificación

- Se revisó la ejecución de `fix-error`: el fallo observado fue la descarga de
  `org.robolectric:android-all-instrumented`, antes de ejecutar las aserciones.
