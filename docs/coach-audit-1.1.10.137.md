# Auditoría de Coach 1.1.10.137

## Salida del visor del hub

La cabecera del visor incluía un título sin restricción de ancho y un cierre dentro del mismo contenido desplazable. El título podía consumir el ancho del botón en pantallas estrechas; al desplazarse, ambos salían de la zona visible.

Se reserva espacio para un botón explícito «Volver al hub», con altura mínima de 48 dp. La cabecera queda fuera del contenido desplazable. El botón ejecuta el cierre existente y devuelve el control al hub. Cuando existe un controlador de navegación Android, Atrás cierra primero la inspección de un recorte y después el visor. El servicio flotante conserva la salida visible, sin depender de un controlador de actividad.

También se reserva el espacio del cierre de la inspección de recortes y se amplía su área de contacto.

Se retiran por completo la tarjeta de calibración del umbral de similitud, el deslizador, los botones de incremento/reducción y sus llamadas de modificación. Se mantiene el funcionamiento del reconocimiento y su información de diagnóstico.

## Portugués

Se corrigen 95 etiquetas detectadas al revisar textos de interfaz, categorías y estados. Entre ellas: imagen, reproducción, perfil del invocador, descripción, revocación, disponibilidad, procesamiento, coordenadas y estados de mensajes. Se corrigen ocho frases que mezclaban «Esencia Naranja» con texto portugués.

Quince textos literales de interfaz y accesibilidad que se mostraban directamente ahora usan el traductor reactivo, incluidos navegación, imágenes adjuntas y elementos del perfil. Se amplía la protección del catálogo para detectar los fragmentos españoles encontrados y se añaden ejemplos de estados dinámicos y suscripciones.

## Validación

- 11 pruebas JVM aprobadas: consejos de elementos, catálogo portugués y generación de análisis en ambos idiomas.
- Auditoría sobre los modelos y generador reales con adaptadores JVM de sus dependencias Android: 142 campeones, 710 análisis por línea y 2.770 campos localizados sin los fragmentos españoles controlados.
- Revisión sintáctica Kotlin y `git diff --check` sin errores.
- Se añaden dos pruebas Compose/Robolectric para el botón visible después de desplazar una pantalla estrecha, retorno al hub y texto portugués con fuente ampliada.

No se dispone de Android SDK en este entorno. No se ejecutaron las dos nuevas pruebas de interfaz ni se compiló una APK. La auditoría de contenido no certifica el 100 % de la interfaz visual ni reemplaza una revisión en dispositivo.

## Pasos de revisión en dispositivo

1. Entrar al visor desde el hub en vertical y horizontal; «Volver al hub» debe estar visible.
2. Desplazar el visor hasta el final y regresar con el mismo botón.
3. Confirmar que no existe la calibración del umbral de similitud.
4. Abrir la inspección de un recorte y comprobar que su cierre tiene espacio suficiente.
5. Cambiar a portugués y revisar perfil, medios, mensajes, suscripciones y estados del visor. Repetir con fuente ampliada y volver a español.

## Flujo de GitHub

El flujo existente de compilación se ejecuta también al abrir o actualizar un PR dirigido a main. Incluye las pruebas de navegación del visor y auditoría portuguesa, compila la APK y la adjunta a la ejecución. La publicación de una versión se limita a main; las validaciones de PR no reemplazan versiones publicadas. El resultado de la primera ejecución debe revisarse antes de integrar el cambio.
