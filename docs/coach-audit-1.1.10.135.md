# Auditoría Coach 1.1.10.135 (851)

## Correcciones

- Al pulsar un objeto principal o situacional se muestra su consejo específico de la build, bajo «Consejo del coach». Si falta ese consejo se utiliza el del catálogo, respetando el idioma seleccionado.
- Runas y hechizos muestran su descripción del catálogo y, por separado, el consejo del elemento pulsado. Las runas situacionales muestran la condición que justifica el cambio. Cuando una build no aporta una nota de runa o hechizo se conserva su consejo general como alternativa.
- El consejo específico no reemplaza estadísticas, pasivas ni descripciones mecánicas del catálogo.
- El detalle de una build y el editor sustituyen el contenido del creador mientras están abiertos. El panel principal y el perfil del creador dejan de dibujarse encima del detalle. Cerrar o volver restaura el contenido anterior dentro de la ventana de pantalla completa.

## Portugués

- Añadidas 362 claves de traducción y corregidas 27 entradas existentes. Incluyen etiquetas completas y plantillas dinámicas de planes, temas, creadores, multimedia, estados, consejos, roles Flex, runas y objetos situacionales. Algunas etiquetas compartidas entre español y portugués se registraron también sin cambios de texto.
- Corregidas frases parcialmente traducidas que conservaban gramática española, aunque una sustitución de palabras hacía que la auditoría anterior las considerase traducidas.
- Las descripciones de runas y hechizos usan sus métodos de localización, y los nombres y etiquetas accesibles de las ventanas de detalle respetan el idioma.
- La auditoría reproducible `tools/PortugueseAudit.kt` añade `--all`: recoge todas las cadenas Kotlin y todos los valores de los datos JSON, incluidas etiquetas almacenadas en modelos de interfaz. Uso: raíz del proyecto, archivo de salida y `--all`.
- Inventario completo: 9.161 cadenas Kotlin y 16.478 frases totales. Los 7.404 resultados sin cambio incluyen identificadores, rutas, expresiones de búsqueda, idiomas alternativos, nombres propios y términos compartidos; este número no representa traducciones faltantes. La revisión se concentró en las cadenas visibles de interfaz, consejos y frases con indicadores de español.
- Nueva prueba revisa ambos catálogos portugueses para detectar fragmentos españoles y traducciones parciales, evitando confundir los pronombres portugueses terminados en `-los` o `-las` con palabras españolas.

## Validación

- Siete pruebas JVM correctas: selección del consejo por elemento, puntuación y mayúsculas en nombres de runas, notas vacías, todas las descripciones de builds incluidas, controles y catálogos portugueses.
- Sintaxis Kotlin correcta en los seis archivos revisados.
- Diferencias sin errores de formato.

## Pendiente en Android

Este entorno no dispone del SDK Android; falta compilar y ejecutar la APK. Comprobar el consejo de cada tipo de elemento, cambiar entre builds sin arrastrar el consejo anterior, abrir una build desde la lista y desde el perfil del creador, regresar con el botón atrás, y recorrer las secciones con portugués seleccionado. La auditoría estática no certifica la traducción visual del 100 % ni traduce automáticamente texto libre creado por usuarios.
