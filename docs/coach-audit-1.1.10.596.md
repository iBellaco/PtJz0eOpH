# Coach 1.1.10.596 (1312) · Ventana de paleta Custom adaptable

La ventana de selección de colores del tema `Custom` queda limitada a una
altura segura de la pantalla, conserva el desplazamiento interno de la paleta
y mantiene la vista previa actualizada antes de aplicar.

## Cambios

- Se ajustó el tamaño de la ventana de color para que sea visible en pantallas
  pequeñas, tablets y distintas densidades.
- Se mantiene una paleta independiente para Primario, Secundario, Resplandor y
  Suplente.
- Cada selección continúa actualizando la vista previa sin guardar el tema
  hasta pulsar `Aplicar`.
- `Aplicar` sigue bloqueado hasta completar los cuatro colores.
- Se actualizó la versión visible de Coach a `1.1.10.596` (1312).

## Verificaciones

- Se revisó el fallo de visibilidad reportado por la prueba de la ventana
  modal.
- La compilación Kotlin y la validación completa quedan a cargo de los flujos
  de Actions antes de entregar el APK.
