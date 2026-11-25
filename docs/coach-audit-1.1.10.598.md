# Coach 1.1.10.598 (1314) · Ventana Custom integrada al panel

La selección de colores del tema `Custom` ahora se muestra como una ventana
superpuesta dentro del panel de temas. Esto evita que una segunda ventana del
sistema quede oculta en determinadas configuraciones y mantiene el selector
visible en teléfonos y tablets.

## Cambios

- La ventana de color se integra al mismo panel de temas y bloquea las acciones
  del fondo mientras está abierta.
- Se conserva una paleta completa e independiente para Primario, Secundario,
  Resplandor y Suplente.
- La ventana se adapta a la altura disponible y permite desplazarse en
  pantallas pequeñas.
- La vista previa sigue cambiando antes de aplicar y `Aplicar` permanece
  bloqueado hasta completar los cuatro colores.
- Se actualizó la versión visible de Coach a `1.1.10.598` (1314).

## Verificaciones

- La prueba de interfaz comprueba la ventana integrada y su título visible.
- La compilación y la prueba completa de Android se ejecutan antes de publicar
  el APK.
