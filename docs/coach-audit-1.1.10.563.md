# Coach 1.1.10.563 — corrección de validación de consejos y temas Premium

## Diagnóstico

- La validación completa no podía conservar la última frase de un consejo ancho con texto grande porque la vista de consejos se iniciaba resumida incluso en el componente de ancho completo.
- La pantalla de temas Premium agregada debajo del explorador de regiones aumentó el contenido vertical y dejó fuera de la ventana el selector de Aguas de Sentina al desplazarse al final.

## Correcciones implementadas

- Los campos de sinergia y counter del draft siguen iniciando compactos y se expanden de forma individual; el componente de consejo completo conserva todas sus líneas y su última instrucción cuando se usa como superficie de lectura.
- Los temas White, Black, Aurora, Ember, Sakura, Forest y Cobalt mantienen sus insignias Premium, muestras de color y vista previa. Sus tarjetas son más compactas para que el explorador regional siga siendo accesible al final del desplazamiento.
- Se redujo únicamente el espacio accesorio del carrusel regional y se mantuvo la legibilidad de los nombres, etiquetas y descripciones de los temas de aplicación.
- Se incrementó la versión de producción a `1.1.10.563`.

## Verificaciones

- `git diff --check`.
- Validación de sintaxis del catálogo JSON de traducciones.
- La prueba de legibilidad renderizada y la auditoría portuguesa deben confirmarse en la validación Android con SDK y emulador.
