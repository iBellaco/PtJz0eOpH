# Coach 1.1.10.437

- Se separaron las resoluciones administrativas de patrocinio de la ranura antigua de operaciones por usuario, evitando conflictos con solicitudes pendientes anteriores.
- Al resolver una publicación se sincroniza también la solicitud original de la cola, evitando que el patrocinador vea un estado atrasado.
- Se redujo la frecuencia adaptativa de captura y análisis cuando no hay un turno activo, conservando una cadencia más rápida durante las selecciones.
- La actividad de usuarios muestra el estado en línea y, cuando corresponde, la última conexión registrada.
- Integraciones externas identificadas: Firebase para autenticación, base de datos, almacenamiento, funciones y App Check; GitHub para consultar actualizaciones; QRServer para generar códigos QR; CDN de Riot/Data Dragon y CommunityDragon para recursos del catálogo; y fuentes web de WildRiftFire/WildRiftCore para referencias del catálogo. La detección de pantalla se ejecuta localmente mediante MediaProjection, ImageReader y LiteRT/MediaPipe, sin una API remota de inteligencia artificial.
