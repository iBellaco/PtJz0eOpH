# Auditoría de Coach 1.1.10.621

- `versionCode`: 1337
- `versionName`: 1.1.10.621

## Correcciones incluidas

- Se eliminó la superficie circular genérica que se superponía a casi todos los estilos de la barra de navegación. Cada estilo conserva ahora un indicador propio: cápsula, mosaico rúnico, subrayado, onda, burbuja de mensaje, prisma, cinta o lente según corresponda.
- Se redujo y alineó la geometría de las 26 variantes: iconos, etiquetas e indicadores comparten una fila compacta y simétrica, sin espacios vacíos innecesarios.
- Se ajustaron las animaciones elevadas para que permanezcan dentro de su área visible y ya no recorten figuras en los bordes de la barra.
- Las vistas previas de temas ya no heredan los márgenes de la barra del sistema. La barra real conserva únicamente el margen inferior necesario para los gestos del dispositivo.
- Se añadió una prueba de interfaz que comprueba que las 26 vistas previas se mantienen compactas y que sus cinco destinos continúan visibles y pulsables.

## Verificación

- `:app:testDebugUnitTest --tests com.example.NavigationCustomizationRenderedTest`: correcto (11 pruebas, incluidas las 26 variantes visuales y su geometría compacta).
