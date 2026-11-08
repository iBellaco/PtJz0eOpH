# Coach 1.1.10.494 — Publicación visible de anuncios patrocinados

## Diagnóstico

- La aprobación administrativa confirmaba el anuncio en Firestore, pero la
  caché local podía conservar el mismo anuncio como pendiente y mantenerlo
  deshabilitado en la pantalla de inicio.
- La sincronización de patrocinadores intentaba escribir esa caché privada de
  vuelta en el registro público completo; un dispositivo con datos antiguos
  podía sobrescribir la publicación recién aprobada.

## Corrección

- La sincronización explícita de anuncios usa una lectura autoritativa del
  servidor después de una aprobación.
- El espejo local reemplaza una fila pendiente por la versión aprobada y nunca
  permite que una fila pendiente vuelva a ocultar una publicación activa.
- Se eliminó la escritura del espejo privado sobre `system_config/app_notices`;
  el registro público solo se modifica mediante la publicación administrativa.
- Se añadió regresión automatizada para aprobación visible y protección contra
  una caché pendiente obsoleta.
- Se incrementó la versión de producción a `1.1.10.494` (`1210`).

## Verificación

- Pruebas unitarias de la política de fusión de anuncios patrocinados.
- Compilación Kotlin y pruebas Android disponibles en la validación de la rama.
- Suite de Firestore y auditorías de publicación se mantienen como requisitos
  de la ejecución de `fix-error` y `coach-validacion`.

## Alcance

No se publica ni fusiona en `main` desde este cambio. La corrección se entrega
primero en `fix-error` y queda sujeta a la validación posterior.
