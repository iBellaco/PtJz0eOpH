# Coach 1.1.10.594 (1310) · Selección funcional del tema Custom

Se corrigió la interacción de las cuatro paletas de colores del tema `Custom`.
Los círculos de color ahora utilizan un control táctil Material con un área
mínima de 48 dp, conservan el estado visual seleccionado y actualizan la
sección correspondiente al tocarse.

## Cambios

- Primario, Secundario, Resplandor y Suplente registran el toque directamente.
- Cada selección muestra el estado `Seleccionado` y el borde de confirmación.
- Se reforzó la prueba de interfaz para comprobar la acción táctil y el estado
  de cada una de las cuatro paletas antes de aplicar el tema.
- Se actualizó la versión visible de Coach a `1.1.10.594` (1310).

## Verificaciones

- Se revisó que cada color exponga una acción de clic y un objetivo táctil de
  48 dp.
- La prueba de interfaz comprueba las cuatro selecciones individualmente.
- La compilación completa se ejecutará en la validación de Android de esta
  revisión.
