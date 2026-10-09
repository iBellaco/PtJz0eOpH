# Coach 1.1.10.234 — código 950

## Cambios

- Las operaciones Premium intentan el servicio directo autenticado y conservan la cola privada como respaldo. Cuando se usa el respaldo, la aplicación sigue observando el resultado hasta seis minutos en vez de abandonar a los cinco segundos.
- El flujo de publicación despliega el servicio directo junto a los índices de la cola cuando existe una identidad de publicación configurada.
- Retirar Premium elimina también Moderador si está asignado como rol secundario. El rol principal distinto de Moderador se conserva.
- Otorgar o revocar verificación y reiniciar dispositivos ahora piden confirmación. El reinicio advierte que cerrará las sesiones existentes.
- Los días Premium personalizados se limitan a 3.650, igual que el servicio.
- La cola deja de procesar cuando faltan sus índices ordenados. Así evita relegar solicitudes antiguas por una lectura sin orden.
- `UserDeviceManagementCard.kt` separa la gestión de dispositivos del diálogo principal para que sus acciones y confirmación puedan evolucionar de forma aislada.

## Verificación

- 44 pruebas del servicio económico, incluida la cola sin índices y Moderador como rol secundario.
- 56 escenarios de permisos y las pruebas de integración pasaron en el emulador local.
- Auditoría de portugués, actualización del catálogo y formato pasaron.
- La compilación local quedó pendiente porque el entorno no permitió descargar Gradle; la comprobación de entrega compilará la variante release y validará el APK firmado.

## Resumen para testers

Coach 1.1.10.234: comprobar que las operaciones Premium se confirman en pantalla o muestran claramente que siguen pendientes. Verificar que retirar Premium elimina Moderador como rol principal o secundario según corresponda. Comprobar las confirmaciones de verificación y reinicio de dispositivos, y que los días personalizados no aceptan más de 3.650.
