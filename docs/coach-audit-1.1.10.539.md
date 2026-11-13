# Auditoría Coach 1.1.10.539

- `versionCode`: `1255`.
- `versionName`: `1.1.10.539`.

## Correcciones de esta revisión

- Se corrigió la compilación del diálogo de reportes: las cadenas nuevas se
  resuelven dentro del contexto composable y se reutilizan en las acciones.
- La burbuja minimizada del overlay conserva la posición exacta donde se suelta
  y puede recorrerse por toda la pantalla dentro de los límites visibles.
- Se mantiene el objetivo inferior para cerrar el overlay mediante un arrastre
  explícito; desaparece el ajuste automático al borde que impedía dejar la
  burbuja en una posición libre.

## Verificación

- Se revisó el error de compilación reportado por la validación anterior.
- Se ejecutaron comprobaciones de formato, recursos localizados y JSON.
