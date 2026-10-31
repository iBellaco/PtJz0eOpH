# Auditoría de portugués: Coach 1.1.10.143 (859)

## Correcciones

- Se unificó la carga del catálogo de la aplicación y las auditorías: textos principales, soporte, correcciones de interfaz y alias. La auditoría anterior omitía el catálogo de soporte.
- Se añadieron 47 entradas para duraciones, estados, cuenta principal, recomendaciones, avatares, rarezas, clasificaciones y avisos de Pix. Se corrigió el encabezado del informe de monetización.
- El componente compartido de imágenes traduce el nombre accesible y el texto de respaldo antes de obtener las iniciales. Las imágenes de esencias también traducen sus descripciones condicionales.
- El overlay traduce el nombre del campeón o el texto alternativo antes de construir la etiqueta con el rol.
- La notificación de descarga de Pix traduce su título y descripción.

## Evidencia y alcance

La nueva auditoría de puntos de salida analiza llamadas de texto, avisos nativos, descripciones de imágenes y componentes que traducen sus parámetros. Incluye literales dentro de condiciones e interpolaciones. Usa el mismo catálogo que la aplicación.

Resultado local: 2.427 apariciones revisadas; cero literales con traducción conocida mostrados sin pasar por la traducción. Las 18 expresiones de texto sin literales ni una llamada directa de traducción se revisaron por su origen: nombres localizados del catálogo, recursos localizados de información, introducción y documentos legales, nombre de evolución de objetos, avisos localizados de streamers y nombres/URL escritos por sus autores.

El inventario amplio revisó 9.253 cadenas de Kotlin y 16.570 cadenas en total, incluyendo los datos incluidos. Las 7.367 cadenas sin cambios no equivalen a errores: incluyen claves, patrones, URL, marcas, nombres propios, textos ya portugueses, palabras compartidas entre los dos idiomas y código de datos sin una ruta de presentación activa. Por ejemplo, los títulos de `ChampionRunePair` no tienen consumidores de presentación; la ruta activa utiliza las listas de runas. El inventario no prueba por sí solo que una cadena llegue a una pantalla.

Ambas variantes de recursos portugueses contienen las 37 claves de los recursos predeterminados. Las pruebas exigen cobertura completa y ausencia de los indicadores de español revisados en sus valores.

## Verificación reproducible

```bash
bash tools/audit-portuguese.sh
gradle :app:testDebugUnitTest --tests com.example.OfflinePortugueseAuditTest --tests com.example.LocalizationSurfaceTest
```

El primer comando requiere Java y curl; descarga dependencias públicas con versiones fijas, genera los inventarios y falla ante literales que evitan la traducción. El proceso de compilación ejecuta esta auditoría antes de las pruebas y de generar el APK.

Las pruebas de idioma verifican el catálogo completo, las 300 builds incluidas y sus consejos, los textos condicionales, las frases con datos dinámicos, los recursos de información y legales, y la accesibilidad e iniciales de imágenes al cambiar español → portugués → español. La compilación conserva las pruebas de conversaciones y streamers.

## Lo que falta comprobar manualmente

- Recorrer el APK actualizado en teléfonos reales con los distintos roles y tamaños de pantalla, incluyendo el overlay y las notificaciones del sistema.
- Revisar la redacción portuguesa con una persona nativa: los indicadores de español y las pruebas no certifican toda la gramática.
- Revisar los avisos y mensajes remotos escritos por personas. La auditoría inspecciona el código y los datos incluidos en el APK; no accede a todas las cuentas o a todo el contenido publicado. Los nombres de canales y sus URL conservan el texto de sus autores.

No se considera esta auditoría una certificación del 100 % de todos los dispositivos o de todo el contenido remoto. Las correcciones de soporte y streamers de la versión anterior se conservan; esta entrega no modifica sus reglas de acceso.
