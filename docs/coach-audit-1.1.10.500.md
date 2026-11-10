# Auditoría de publicación de patrocinios y consumo en reposo de Coach 1.1.10.500

## Versión

- `versionName`: `1.1.10.500`
- `versionCode`: `1216`
- Alcance: confirmación de anuncios patrocinados, consistencia de Inicio y reducción de trabajo innecesario cuando el asistente está inactivo.

## Correcciones aplicadas

- Si una solicitud de patrocinio aún no generó el anuncio, la decisión solicita una resolución inmediata y solo muestra éxito después de confirmar el documento público desde el servidor. Una operación todavía en cola queda informada como pendiente, no como anuncio visible.
- Tras esa confirmación, el anuncio se incorpora inmediatamente al Inicio del dispositivo actual, sin esperar otro evento de sincronización.
- Las respuestas tardías de caché ya no sustituyen una lista de anuncios más reciente confirmada por el servidor.
- Las instantáneas de avisos sin cambios ya no reescriben la caché local ni provocan recomposición del feed.
- Inicio conserva sus escuchas en tiempo real y evita repetir lecturas y consultas privadas cada vez que se recompone.
- El botón principal no ejecuta cuatro animaciones infinitas mientras el asistente está detenido.
- El fondo global conserva degradados suaves, pero deja de aplicar desenfoque y animaciones continuas a pantalla completa.
- La precarga queda limitada a dos miniaturas de imagen. Los videos y medios ampliados se descargan al abrirlos, evitando transferencias repetidas en segundo plano.
- El cálculo temporal del panel de patrocinio se actualiza al cambiar el minuto, en lugar de hacerlo seis veces por minuto.
- No se agregaron perfiles por modelo; las coordenadas de escáner guardadas por dispositivo permanecen intactas.

## Pruebas

- `:app:testDebugUnitTest` para `SponsorReviewPolicyTest` y `SponsorNoticeMergePolicyTest`: correctas.
- Emulador de reglas `sponsor-review.test.mjs`: correcto; verifica la publicación legible por otro usuario y el rechazo de escrituras no autorizadas.
- Emulador de economía `emulator.integration.mjs`: 31 casos correctos, incluida la aprobación de un anuncio aún en cola con un solo cargo al reintentar.
- `git diff --check`: correcto.
- Pendiente de comprobar el recorrido completo con dos cuentas reales y una publicación nueva en un dispositivo físico; no se infiere de las capturas aportadas.
- Pendiente de medir duración de batería y fluidez en un dispositivo físico; las optimizaciones de GPU, red y precarga no se traducen aquí en una cifra estimada.
