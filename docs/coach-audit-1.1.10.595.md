# Coach 1.1.10.595 (1311) · Selector modal por sección del tema Custom

El tema `Custom` ahora abre una ventana independiente al tocar Primario,
Secundario, Resplandor o Suplente. Cada ventana muestra la paleta completa de
colores de esa sección y mantiene abierta la selección para poder probar
alternativas antes de aplicar el tema.

## Cambios

- Se reemplazaron las paletas simultáneas por cuatro controles independientes.
- Cada control abre únicamente los colores de su propia sección.
- La selección actualiza inmediatamente la vista previa del tema sin guardar
  todavía el cambio definitivo.
- `Aplicar` continúa bloqueado hasta seleccionar los cuatro colores.
- Se añadieron las traducciones portuguesas de la nueva ventana y sus estados.
- Se actualizó la versión visible de Coach a `1.1.10.595` (1311).

## Verificaciones

- La compilación Kotlin de depuración pasó.
- La prueba de interfaz comprueba que cada sección abre su ventana, muestra la
  paleta completa y registra la selección.
- La validación de producción queda a cargo de la ejecución de `fix-error` y
  de la revisión completa de `coach-validacion`.
