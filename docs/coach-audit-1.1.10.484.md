# Coach 1.1.10.484 — build 1200

## Correcciones

- Las operaciones de esencias y las decisiones de anuncios pendientes se
  procesan al recibir la solicitud, sin esperar al ciclo periódico de respaldo.
- Los avisos de recompensas se publican como mensajes nuevos de la bandeja de
  entrada, con orden temporal estable y estado no leído.
- La bandeja conserva el aviso de mensaje mientras terminan de sincronizarse
  sus fuentes, evitando perder la animación o el contador.
- Se añadió RPM efectivo junto con CPM en el panel de monetización, el panel de
  patrocinadores, las tarjetas por anuncio y los reportes copiados.

## Verificación

- `server/economy`: 46 pruebas unitarias correctas.
- Integración aislada con emuladores: 30 casos correctos.
- `node --check` para las funciones de economía y `git diff --check` correctos.
- La prueba Android local queda pendiente porque este entorno no tiene SDK de
  Android; debe completarse en la validación remota de producción.
