# Auditoría Coach 1.1.10.428

## Corrección

- Las solicitudes de patrocinio tienen una sección visible para revisión, con acciones separadas para aprobar o rechazar.
- El rechazo actualiza el estado, retira el anuncio y devuelve una sola vez las Esencias Azules cobradas mediante una operación confiable e idempotente.
- Los anuncios pendientes ya no se duplican como solicitudes económicas genéricas en el buzón ni mantienen el contador de navegación después de leerlos o eliminarlos.
- Se retiró de la pantalla de perfil la entrada para canjear Esencia Naranja.

## Verificación

- `versionCode`: 1144.
- `versionName`: `1.1.10.428`.
- La validación de políticas JavaScript pasó; la compilación Android no pudo ejecutarse localmente porque el wrapper de Gradle no estaba disponible y el entorno no permitió descargarlo.
