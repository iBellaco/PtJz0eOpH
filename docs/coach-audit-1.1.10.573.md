# Coach 1.1.10.573 (1289) · Estabiliza la resolución de KSP en validaciones limpias

## Corrección

- Se añadió una regla de resolución de plugins para que KSP use directamente
  `com.google.devtools.ksp:symbol-processing-gradle-plugin` con la versión
  declarada en el catálogo.
- La regla evita depender del marcador de plugin cuando el runner de CI parte
  de una caché Gradle vacía. Se conserva KSP `2.3.9` y el resto de la lógica del
  flujo permanece sin cambios.

## Verificación

- Se comprobó que KSP `2.3.9` está publicado en Maven Central.
- Se incrementó la versión de producción a `1.1.10.573` (1289).
- Se validará la resolución y las pruebas selectivas en el flujo de `fix-error`.
