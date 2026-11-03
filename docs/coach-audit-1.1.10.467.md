# Coach 1.1.10.467 — build 1183

## Decisiones de anuncios patrocinados

- Aprobar o rechazar ya no mantiene la ventana bloqueada mientras el mecanismo
  de recuperación completa una decisión pendiente.
- La acción directa usa un límite breve y, si necesita recuperación, conserva
  la misma operación sin duplicar aprobaciones, cargos ni reembolsos.
- La solicitud se retira mediante la sincronización en tiempo real cuando la
  decisión queda confirmada.
- Una aprobación sincroniza el anuncio publicado y un rechazo conserva el
  cierre seguro con devolución de las esencias cobradas.
- La acción pulsada muestra progreso y ambos controles vuelven a estar
  disponibles si todavía no existe una confirmación final.

## Verificación

- Regresión unitaria para impedir que una decisión respaldada mantenga la
  interfaz esperando al proceso programado.
- Pruebas de resolución, publicación, rechazo, reembolso e idempotencia del
  servicio económico.
- Compilación y validación de recursos en español y portugués.
