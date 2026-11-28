# Auditoría de Coach 1.1.10.623

- `versionCode`: 1339
- `versionName`: 1.1.10.623

## Mejoras incluidas

- Las recomendaciones del draft ahora muestran etiquetas de texto con color según su contexto táctico: counter, sinergia, prioridad, favoritos o riesgo. Se eliminaron los iconos decorativos de esas etiquetas y de las tarjetas de sinergia.
- La puntuación reconoce combinaciones reales entre el campeón recomendado y los aliados ya elegidos. Una combinación entre otros carriles no altera ni aparece como recomendación para una línea ajena.
- El Hub abre el selector de campeón con la línea del espacio tocado ya filtrada, reinicia correctamente la búsqueda al cambiar de espacio y amplía el botón para quitar un campeón seleccionado.
- El filtro de favoritos del Hub conserva su texto completo en pantallas estrechas mediante una altura mínima, dos líneas útiles y un interruptor con área de toque estable.
- Se añadieron efectos estacionales de Navidad y Año Nuevo únicamente en la aplicación. Cada efecto se puede activar, desactivar o programar con un rango de fechas locales, por lo que se muestra según el calendario y la zona horaria de cada dispositivo.
- Se incorporó una pantalla de configuración para administrar ambos efectos y se publicó el acceso de solo lectura necesario para que el calendario llegue a todos los dispositivos.

## Verificación

- `:app:compileDebugKotlin`: correcto.
- Se añadieron pruebas para los rangos locales de Navidad/Año Nuevo, el filtro inicial de línea del Hub, la relación exclusiva entre una recomendación y su propio combo aliado y el acceso público de solo lectura al calendario publicado.
- La ejecución local de pruebas unitarias quedó bloqueada por la imagen de ejecución: solo dispone de un entorno Java sin compilador (`javac`). La validación automatizada de la revisión conserva esas pruebas para ejecutarlas en el entorno de compilación completo.
