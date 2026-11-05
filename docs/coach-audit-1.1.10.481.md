# Coach 1.1.10.481 — build 1197

## Ajustes solicitados

- Igualé el ancho y la altura táctil de Guardar y Vaciar en el Hub del draft.
- Retiré del área superpuesta del draft el banner de estado y los textos de
  certeza/roles; el Hub conserva únicamente su contenido y controles.
- Corregí la reconciliación de moderación: las representaciones antiguas de un
  mismo anuncio patrocinado ya no sobreviven a una aprobación o rechazo. Los
  registros de soporte no patrocinados siguen visibles.

## Verificación

- La prueba del emulador de reglas de patrocinio pasó aprobación/publicación,
  rechazo/reembolso, repetición idempotente y protección de anuncios reembolsados.
- Se añadieron pruebas de identificación compartida entre filas de patrocinio
  en vivo, legado y publicación.
- La prueba de navegación del Hub ahora verifica que los textos retirados no se
  renderizan.
- La compilación Android completa queda pendiente de ejecutar en Actions; este
  entorno local no tiene el SDK Android.
