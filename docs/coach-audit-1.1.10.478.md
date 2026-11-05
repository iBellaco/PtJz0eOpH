# Coach 1.1.10.478 — build 1194

## Producción: parche 7.3e y decisiones de patrocinios

- Conserva la actualización obligatoria para versiones de producción de
  `1.1.10.477` y sube el build a `1.1.10.478`.
- Cambia el parche activo de Wild Rift a `7.3e`, sin reinterpretar evidencias
  históricas ni inventar cambios de balance.
- Aceptar o rechazar un anuncio ahora espera el resultado duradero del servicio
  cuando usa la cola de respaldo. El botón deja de devolver inmediatamente un
  falso estado pendiente y la tarjeta se retira al confirmarse la decisión.
- La tarjeta se mantiene pendiente cuando el servidor no confirma el resultado;
  el mensaje ya no afirma que la operación terminó.

## Verificación

- Se actualizaron las pruebas de política de cola de decisiones y del parche.
- Se conserva la lógica de servidor que prueba aprobación, rechazo, reembolso e
  idempotencia; la validación Android completa queda pendiente de Actions.
- La prueba manual final debe pulsar Aprobar y Rechazar en anuncios distintos,
  comprobar que desaparece cada tarjeta al confirmarse y verificar que el anuncio
  aprobado aparece en la lista pública y el rechazado devuelve las esencias.
