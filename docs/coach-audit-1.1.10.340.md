# Auditoría Coach 1.1.10.340

## Cambios

- Los errores del acceso se normalizan a mensajes en español y se traducen al portugués seleccionado; ya no se muestra texto técnico del proveedor.
- La actividad por rol muestra el estado individual en línea o desconectado y conserva el acceso a la gestión del usuario.
- El envío de coordenadas incluye el modelo del dispositivo, conserva la información para soporte y limita los envíos locales a cinco por día.
- El visor flotante puede deshabilitarse globalmente o limitarse a UID autorizados desde la configuración de acceso.
- El indicador de subida o bajada de categoría de la Tier List tiene tamaño, accesibilidad y etiquetas visibles estables.
- Las recomendaciones del primer pick limitan el texto largo para evitar tarjetas desproporcionadas.

## Verificaciones

- JSON de traducciones válido.
- `git diff --check` sin errores.
- La compilación local no pudo iniciarse porque el entorno no tiene la distribución Gradle y la conexión de descarga fue rechazada; la compilación, pruebas Android, APK ofuscado, firma e instalación deben ejecutarse en Actions antes de publicar.

## Pendiente de publicación

- Validar la configuración remota del visor con una cuenta de prueba autorizada y otra no autorizada.
- Ejecutar el flujo completo de CI y publicar únicamente el APK release ofuscado y el ZIP sin secretos.
