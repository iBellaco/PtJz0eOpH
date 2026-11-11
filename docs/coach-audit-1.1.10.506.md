# Coach 1.1.10.506 — Eliminación completa de anuncios y limpieza de solicitudes

## Corrección

- El botón de eliminar ya no modifica solo la caché local.
- Elimina de forma coordinada el anuncio privado, su publicación visible y la solicitud de cola asociada.
- El propietario y el administrador pueden eliminar el anuncio que les corresponde; las reglas siguen impidiendo borrar anuncios ajenos.
- Inicio toma la publicación pública como fuente para no resucitar un anuncio eliminado desde una entrada antigua del registro compartido.
- El indicador de solicitudes excluye una solicitud de patrocinio cuando su anuncio ya está aprobado, rechazado o reembolsado.
- La ruta administrativa sin facturación conserva el identificador de cola para permitir su limpieza posterior.

## Versión

- `1.1.10.506`, build `1222`.

## Verificación

- Prueba de patrocinio: correcta, incluida eliminación del anuncio público por su propietario y rechazo de eliminación por otra cuenta.
- Reglas Firestore: 65 escenarios correctos.
- Pruebas Android dirigidas: correctas.
- Economía: 52 pruebas unitarias correctas.
- Compilación Android: pendiente de ejecutar después de este cambio.
