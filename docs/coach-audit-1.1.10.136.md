# Auditoría de Coach 1.1.10.136

## Cambios

- La ficha del campeón conserva la gráfica grande de evolución de la tasa de victoria. Se elimina únicamente la gráfica pequeña del encabezado.
- Los objetos evolucionados muestran la etiqueta y el objeto de origen en un bloque propio. Ambos textos pueden ocupar varias líneas sin competir por ancho con el nombre o la categoría. El objeto de origen se resuelve desde el catálogo y se localiza.
- Las pantallas y ventanas que consultaban directamente `LocalLanguage.current` usan `currentAppLanguage()`: la misma lectura reactiva que las etiquetas, con respaldo en la selección persistente cuando no existe un proveedor y normalización de `pt-BR` a `pt`. Se conserva el proveedor de previsualización de la pantalla de selección de idioma.
- El análisis táctico portugués usa las líneas y habilidades traducidas. Antes insertaba `activeRole.displayName`, `su H1` y `su Definitiva` en frases portuguesas.
- Las búsquedas de runas y hechizos consultan los nombres, descripciones y categorías traducidos y recalculan sus resultados al cambiar de idioma.
- Se corrigen dos entradas menores del catálogo portugués.

## Validación ejecutada

- 10 pruebas JVM aprobadas: consejos de elementos, catálogo portugués y análisis táctico en las cinco líneas, retorno al español y habilidades ausentes.
- Auditoría de contenido ejecutada sobre los modelos y generador reales, con adaptadores JVM para recursos gráficos, traducción y repositorio: 142 campeones, 710 análisis por línea y 2.770 campos localizados. Ninguno contiene los fragmentos españoles controlados. Los seis objetos con origen de evolución resuelven su origen correctamente.
- Sintaxis Kotlin revisada con PSI en los archivos modificados; `git diff --check` sin errores.
- Auditoría ampliada de textos: 4.255 frases examinadas, 29 sin cambios. Se revisaron sus usos: expresiones regulares, alias de búsqueda y términos compartidos entre idiomas. No se contabilizan como traducciones faltantes por permanecer iguales.

## Límites y pruebas de interfaz pendientes

No se dispone de Android SDK en este entorno. No se compiló una APK ni se ejecutaron pruebas Compose/Robolectric. Las dos nuevas pruebas de interfaz cubren el cambio español → portugués → español sin proveedor de idioma y el proveedor `pt-BR`; deben ejecutarse en un entorno Android.

El control automatizado detecta fragmentos españoles, pero no certifica una traducción visual del 100 % ni la calidad lingüística de cada frase. El contenido escrito por usuarios se conserva.

## Revisión en dispositivo

1. Abrir Hwei: debe aparecer una sola gráfica dentro de su ficha, debajo de las estadísticas.
2. Revisar los seis objetos evolucionados en lista y detalle, con tamaño de fuente normal y ampliado. La etiqueta debe ajustarse sin columnas estrechas ni solapamientos.
3. Cambiar a portugués, recorrer catálogo, fichas, builds, preguntas frecuentes, historial y ventanas flotantes. Revisar líneas, habilidades, objetos de origen y consejos.
4. Buscar runas y hechizos por sus nombres portugueses. Cambiar nuevamente a español y comprobar que la interfaz y los resultados se actualicen.
