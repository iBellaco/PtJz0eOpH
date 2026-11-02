# Coach 1.1.10.449 — build 1165

## Alcance

Se corrigió la presentación de solicitudes de publicaciones que solo existían
en el almacenamiento local. La aplicación ahora confirma el estado contra el
servidor antes de conservar una solicitud antigua; si el servidor no la
reconoce, se elimina del listado. Las solicitudes recién creadas tienen una
ventana breve de protección para evitar que desaparezcan mientras se propagan.

La bandeja de operaciones ya no convierte datos de caché de Firestore en
solicitudes pendientes. Si no hay confirmación en vivo, no muestra una
solicitud como “en espera”. Los reintentos mantienen el mismo identificador y
muestran un estado de procesamiento en lugar de un error genérico.

El texto de la acción de rechazo indica que devuelve las esencias azules y se
mantuvieron las traducciones correspondientes.

## Comprobación

Se añadieron pruebas para caché, estados activos, limpieza de solicitudes
locales antiguas y persistencia de la fecha de creación. La validación de
Android debe ejecutarse en CI porque este entorno no dispone de acceso a la
distribución de Gradle para descargar dependencias.
