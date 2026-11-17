# Coach 1.1.10.564 — calibración Xiaomi y catálogo visual de aplicación

## Correcciones implementadas

- Se añadió el perfil exacto de Vision Draft para Xiaomi `24122RKC7G`, pantalla `2400x1080`, con alias de captura `1920x864` y `1280x576`. Conserva los baneos manuales y las posiciones de slots aportadas en la evidencia, sin redistribuir huecos.
- Se documentaron las coordenadas y la evidencia de detección en `docs/xiaomi-24122rkc7g-vision-draft-calibration.md`.
- El catálogo de temas independientes de regiones pasó a 12 estilos Premium. El carrusel usa el mismo orden visual que los temas regionales y muestra candado en lugar de una etiqueta amarilla Premium para cuentas sin acceso.
- Black dejó de compartir la apariencia de Piltover; también se reemplazaron las combinaciones de color repetidas por paletas propias con texto de alto contraste y se añadieron Graphite, Mint, Volt, Candy y Slate.
- El botón de información de la pantalla principal conserva su acción y ahora tiene pulso de escala, borde y halo animado para hacerlo visible.

## Verificaciones

- Prueba de perfil exacto, aliases de captura, posiciones de baneos manuales y rechazo de modelo/dimensión incorrectos.
- Prueba de catálogo con 12 temas, descripciones únicas y paletas no repetidas.
- `git diff --check`.
- Validación de sintaxis del catálogo JSON de traducciones.
- La compilación Android y la prueba de interacción visual deben confirmarse en el flujo selectivo de `fix-error`.
