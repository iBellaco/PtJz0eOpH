# Coach 1.1.10.597 (1313) · Validación del contenido visible de la paleta

Se corrigió la validación automatizada de la ventana de colores `Custom` para
comprobar el contenido visible del diálogo y la selección real de colores,
incluyendo la paleta completa de cada sección.

## Cambios

- La prueba identifica la ventana por su contenido y su semántica de diálogo,
  en lugar de exigir visibilidad al contenedor raíz de la ventana en
  Robolectric.
- Se conserva la ventana adaptable con desplazamiento interno para Primario,
  Secundario, Resplandor y Suplente.
- Se mantiene la actualización inmediata de la vista previa y el bloqueo de
  `Aplicar` hasta completar los cuatro colores.
- Se actualizó la versión visible de Coach a `1.1.10.597` (1313).

## Verificaciones

- La compilación Kotlin queda verificada localmente.
- La prueba completa de Android debe confirmar la interacción de las cuatro
  paletas antes de publicar el APK.
