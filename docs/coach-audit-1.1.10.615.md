# Auditoría de Coach 1.1.10.615

## Cambios incluidos

- El panel de reportes mantiene el desplazamiento vertical habilitado, conserva una lista estable y muestra cada fecha y hora dentro de un marco identificable.
- La actividad de moderación muestra acción, fecha y hora, usuario afectado, identificador, valor anterior, valor nuevo, detalle y solicitud relacionada cuando existe.
- Los anuncios globales incorporan la opción Global, seleccionada por defecto, y publican la misma comunicación para Español y Português.
- La segmentación de anuncios CPM/RPM ya no ofrece General. La selección empieza vacía, exige una opción válida y conserva la visibilidad para ambos idiomas cuando se elige Español o Português.
- El cálculo de precio usa una previsión separada de vistas y clics para Español, Português y la combinación de ambos. La previsión portuguesa parte de la audiencia actual más reducida y no reutiliza el precio español.
- El gestor de anuncios y noticias elimina General, exige idioma de publicación y permite cambiar entre la vista previa en Español y Português cuando se publica para ambos.
- Las paletas regionales se muestran con una barra de cuatro tonos y el texto aplica contraste mínimo para evitar textos ilegibles en temas claros u oscuros.
- El cierre de sesión limpia el marcador local de sesión. Al volver a entrar, una cuenta sin token local se registra como sesión nueva en lugar de cerrarse por un token anterior.
- Se actualizaron las respuestas de FAQ relacionadas con temas, cambio de cuenta y modo visual.
- Se añadieron 50 ideas nuevas de FAQ de uso de la aplicación y 50 ideas troll en un documento separado. No se incorporaron a la interfaz.

## Verificación

- `versionCode`: `1331`.
- `versionName`: `1.1.10.615`.
- `:app:compileDebugKotlin`: correcto.
- Pruebas unitarias puras de anuncios globales, segmentación de patrocinio y catálogo de temas: correctas.
- La tanda que incluye `RuntimeBehaviorTest` queda limitada por la falta local del artefacto Robolectric `android-all-instrumented:7.0.0_r1-robolectric-r1-i7`; el intento offline no pudo descargarlo porque la conexión estaba cerrada.
