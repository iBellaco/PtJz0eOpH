# Coach 1.1.10.599 (1315) · Validación del selector Custom

La personalización de colores del tema `Custom` queda cubierta por una prueba
de interfaz que reproduce el recorrido completo de una persona usuaria.

## Cambios

- Se valida la apertura del selector para Primario, Secundario, Resplandor y
  Suplente desde las dos zonas de acceso del tema.
- La comprobación espera a que la ventana esté colocada y tenga un área útil,
  evitando falsos fallos en pantallas emuladas.
- Se verifica la selección de cada color, el cierre de la ventana y que
  `Aplicar` se habilite únicamente al completar las cuatro elecciones.
- Se actualizó la versión visible de Coach a `1.1.10.599` (1315).

## Verificación

- Prueba de interfaz ejecutada localmente: selector Custom completo,
  selección de cuatro colores y aplicación final del tema.
