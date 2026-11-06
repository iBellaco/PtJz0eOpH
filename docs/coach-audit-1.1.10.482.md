# Coach 1.1.10.482 — build 1198

## Ajustes solicitados

- La vista de solicitudes conserva los anuncios patrocinados recibidos en tiempo
  real cuando termina de cargar las solicitudes antiguas, por lo que una cuenta
  con permisos de gestión vuelve a ver el anuncio pendiente para aprobarlo o
  rechazarlo.
- Las operaciones privilegiadas que quedaron pendientes pueden reintentarse con
  el mismo identificador seguro y no generan un segundo ajuste de saldo. Los
  envíos de anuncios siguen retenidos para moderación.
- El control de ajuste de esencias permanece disponible después de una respuesta
  pendiente para poder reintentar la operación segura sin cerrar el diálogo.

## Verificación

- Se añadieron pruebas de preservación de filas patrocinadas durante la carga
  combinada y de reintento idempotente para operaciones privilegiadas.
- `server/economy` pasó sus 46 pruebas unitarias.
- La prueba Android local quedó pendiente porque este entorno no tiene instalado
  el SDK de Android; se debe ejecutar en la validación de Actions.
