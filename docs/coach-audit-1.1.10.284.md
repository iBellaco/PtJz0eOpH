# Auditoría de cambios Coach 1.1.10.284

## Antes

- La ficha del campeón mostraba la hora de la última muestra histórica. Si la fuente repetía el mismo valor, la hora parecía congelada aunque la consulta automática sí hubiera terminado.
- La línea y el punto de la gráfica usaban siempre cian, incluso cuando el valor comparado bajaba.
- La lista no comunicaba visualmente que un campeón hubiera cambiado de categoría durante la sesión.
- Las preguntas frecuentes mezclaban afirmaciones que no podían verificarse con instrucciones reales y no mostraban un orden serial.

## Después

- `statsCheckedAt` representa ahora cada consulta válida de estadísticas; la historia de 24 y 12 horas continúa usando muestras publicadas, sin inventar variaciones.
- La gráfica y la insignia de cambio usan verde cuando la comparación sube, rojo cuando baja y cian cuando no hay cambio.
- Se añadió un indicador de subida o bajada de tier, calculado contra la categoría anterior de la sincronización.
- Las preguntas frecuentes muestran numeración estable, conservan las preguntas humorísticas al final y fueron reescritas para describir límites y fuentes sin prometer resultados.

## Validación

- Se incrementó `versionCode` de 999 a 1000 y `versionName` a `1.1.10.284`.
- La validación final y la publicación deben ejecutarse en GitHub Actions, que dispone del SDK Android del proyecto.
