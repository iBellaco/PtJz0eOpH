# Coach 1.1.10.312 (build 1028)

## Cambios

- Los consejos de objetos situacionales ya no repiten el campeón y el rol en cada tarjeta.
- La regla de compra se muestra una sola vez en la sección de objetos situacionales, en lugar de copiarse en cada consejo.
- Las amenazas concretas de un objeto se normalizan y deduplican antes de mostrarse.
- La alineación visual del draft ahora ajusta las filas Y de cada slot confirmado, además de la columna X, para adaptarse a recortes, barras y letterboxing del fotograma real.

## Archivos

- `app/build.gradle.kts`
- `app/src/main/java/com/example/util/BuildElementAdvice.kt`
- `app/src/main/java/com/example/service/screen/DraftRingAlignment.kt`
- `app/src/main/java/com/example/ui/screens/ChampionDetailSheet.kt`
- `app/src/test/java/com/example/BuildElementAdviceTest.kt`
- `app/src/test/java/com/example/DraftRingAlignmentTest.kt`

## Validación

- Añadidas regresiones para evitar la repetición del contexto del campeón y de la regla de compra.
- Añadida regresión de alineación de filas con desplazamiento vertical.
- Suite Android y APK release pendientes de ejecutar en el entorno con SDK/Gradle disponible.
