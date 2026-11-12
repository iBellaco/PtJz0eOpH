# Auditoría Coach 1.1.10.528

- `versionCode`: `1244`.
- `versionName`: `1.1.10.528`.

## Evidencia de calibración

- Guardar y enviar activa el visor de calibración antes de tomar la evidencia.
- Cuando MediaProjection está disponible se obtiene una captura literal nueva en el momento del envío, con reintentos acotados y sin iniciar monitoreo adicional.
- La imagen adjunta se anota con las regiones de aliados, rivales, baneos y picks superiores, además de indicar si es captura literal o fotograma de respaldo y si el visor estaba activo.
- Si no hay una proyección disponible se conserva el último fotograma procesado y se declara explícitamente esa limitación en el informe.
- El panel de administración abre los reportes de coordenadas mostrando el texto completo, sin depender de tocar el título, y evita interpretarlos como sugerencias de build.
- Las capturas adjuntas tienen un botón de descarga directo en la tarjeta; la vista ampliada conserva su descarga.
- El visor se activa por defecto al abrir el diálogo de reconocimiento y permanece activable al guardar/enviar.

## Traducción y validación

- Se añadieron las etiquetas de texto completo y evidencia visual al diccionario portugués.
- Se ejecutaron la regeneración/verificación del catálogo de builds, la validación JSON, la auditoría de portugués y `git diff --check`.
- La compilación local no pudo ejecutarse porque este entorno no tiene Android SDK (`local.properties`/`ANDROID_HOME` ausentes); CI debe realizar la compilación Android.
