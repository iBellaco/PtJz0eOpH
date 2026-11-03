# Coach 1.1.10.465 — build 1181

## Solicitudes de anuncios y sincronización

- La reconciliación del estado local ahora conserva únicamente solicitudes
  realmente pendientes, en procesamiento o en revisión.
- Los registros completados, rechazados, reembolsados o ya aprobados dejan de
  reaparecer como solicitudes “por confirmar”.
- La consulta se realiza contra el estado del servidor y no revive datos de la
  caché local ni historiales cerrados.
- La resolución de una solicitud dispone de un margen mayor para completar la
  operación directa antes de pasar al mecanismo de recuperación.

## Verificación

- 76 pruebas auxiliares de validación pasaron.
- 46 pruebas del servicio económico pasaron.
- Se añadió una regresión específica para evitar que registros cerrados
  mantengan el buzón local en estado pendiente.
