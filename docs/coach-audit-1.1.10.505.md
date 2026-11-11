# Coach 1.1.10.505 — Resolución administrativa sin dependencia de facturación

## Alcance

- Permite que una decisión administrativa sobre una solicitud que aún está únicamente en cola se guarde directamente.
- La aprobación publica el anuncio en Inicio y la decisión queda identificada para que el trabajador gratuito no cobre dos veces.
- El rechazo queda marcado como resuelto y deja de mostrarse como pendiente.
- El trabajador programado reconoce estas decisiones y cierra la cola de forma idempotente.
- Se mantienen las comprobaciones de sesión, la autorización administrativa y las reglas de lectura pública.

## Versión

- `1.1.10.505`, build `1221`.

## Verificación

- Pruebas Android dirigidas: correctas.
- Reglas Firestore: 65 escenarios correctos.
- Integración de economía: 31 casos correctos.
- La ruta directa no requiere Cloud Functions para confirmar la decisión administrativa; la conciliación posterior conserva la idempotencia.
- No requiere Cloud Functions para confirmar una decisión ya visible al moderador; la conciliación de la cola continúa usando el trabajador programado.
- No se fusiona automáticamente con la rama principal.
