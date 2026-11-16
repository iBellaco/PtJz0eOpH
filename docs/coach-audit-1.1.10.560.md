# Coach 1.1.10.560 — tier China por línea y subcategorías funcionales

## Cambios

- La tier list China conserva la categoría publicada por Tencent para cada
  campeón y línea, sin reutilizar la línea primaria del catálogo Global.
- La vista China selecciona las estadísticas y el tier de la línea activa;
  cuando se muestran todas, usa la línea China con datos completos y mayor
  prioridad publicada.
- Las cuatro subcategorías de China conservan snapshots independientes y
  descartan la caché anterior que no incluía el tier por línea.
- La subcategoría visible «Grieta del Invocador» se renombra a «Global».

## Verificación

- Pruebas unitarias del parser de Tencent y de selección regional ampliadas
  para comprobar tier y estadísticas por línea.
- `git diff --check` ejecutado correctamente.
- La compilación Android local no pudo iniciarse porque el entorno solo
  expone un runtime Java sin `javac`; queda para la validación oficial.
