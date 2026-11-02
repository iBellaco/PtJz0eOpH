# Coach 1.1.10.460 — build 1176

## Fallo confirmado

La validación 41496375283 mantuvo verdes todos los fragmentos salvo
`native-name-sequence (9)`. El caso `sequence-52-ally-first` reconocía las
nueve selecciones durante la secuencia, pero al leer directamente la captura
`draft-mel-nine.jpg` conservaba una línea de posición obtenida desde la banda
auxiliar y no ejecutaba la recuperación contextual del título corto `VI`.

La evidencia nativa confirmó que el recorte contextual contenía un único
elemento `VI` dentro del área objetivo. Los textos `ORN` y `ORNH` estaban fuera
de esa área y se utilizaban únicamente para que ML Kit detectara el texto corto.

## Cambios

- Registrar qué posiciones proceden de la banda auxiliar de líneas.
- Permitir que esos casos concretos continúen hacia la recuperación contextual
  del título, sin ampliar la búsqueda para posiciones leídas normalmente.
- Dar prioridad a un nombre completo de campeón ubicado dentro del objetivo
  contextual sobre una línea obtenida desde otra banda vertical.
- Mantener la exigencia de un único campeón completo; los textos vecinos nunca
  pueden proporcionar la identidad del puesto.
- Añadir regresión para la sustitución segura de una línea auxiliar por `VI`.
- Actualizar a versión 1.1.10.460, build 1176.

## Alcance

La modificación afecta únicamente la recuperación contextual de títulos
aliados cortos que habían quedado bloqueados por una lectura auxiliar de línea.
No cambia coordenadas por modelo, identidades esperadas, umbrales visuales ni
la detección de los demás fragmentos ya aprobados.
