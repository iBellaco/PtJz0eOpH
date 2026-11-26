# Coach 1.1.10.607 · Temas y builds por línea

## Correcciones

- Se retiró el tema `Custom` del catálogo visible. Las preferencias antiguas
  que todavía lo tenían guardado se migran a Piltover para evitar una selección
  inválida.
- Los doce temas de aplicación usan paletas acromáticas propias, separadas de
  los colores de las regiones, y la vista previa ya no conserva controles del
  editor eliminado.
- El panel de reportes y sugerencias publica primero la lista para mantener el
  desplazamiento disponible mientras se resuelven los estados locales en un
  hilo de trabajo; también reinicia la posición al cambiar pestaña, estado o
  búsqueda y descarta respuestas antiguas.
- Las builds de Soporte se normalizan con exactamente un objeto inicial válido
  (`Hoz espectral` o `Escudo reliquia`) en el núcleo.
- Las builds de Jungla normalizan siempre `Aplastar` (alias de `Castigo`) como
  hechizo obligatorio y conservan un segundo hechizo distinto.

## Verificación

- Se añadieron comprobaciones del catálogo de temas acromáticos y de la
  ausencia de `Custom`.
- Se añadieron comprobaciones para que las builds de Jungla restauren
  `Aplastar` y para que el catálogo oficial mantenga sus reglas por línea.
