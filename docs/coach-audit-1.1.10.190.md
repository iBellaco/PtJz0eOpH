# Auditoría Técnica y Registro de Modificaciones — Versión 1.1.10.190 (Build 906)

## Resumen Ejecutivo
Esta versión implementa la resolución directa y unificada en solicitudes de pago (canjes USDT), la optimización en la marcación individual y general de mensajes leídos en la bandeja de entrada, y la gestión selectiva de notificaciones de historial con marcado automático al abrir y descarte inmediato al cerrar.

## Modificaciones Principales

### 1. Bandeja de Entrada y Solicitudes de Pago (USDT)
- **Resolución unificada en hilo único:** Al procesar un pago de canje de Esencias Naranjas a USDT, la respuesta de confirmación o rechazo se registra como réplica formal dentro del mismo hilo del ticket original (`payment_$id`), en lugar de generar un mensaje separado desvinculado.
- **Transición de estado a RESUELTO:** Las solicitudes de pago completadas actualizan su estado a `RESUELTO` con distintivo visual verde esmeralda sincronizado en tiempo real y multidispositivo.
- **Marcación individual de lectura:** Se incorporó en cada tarjeta de mensaje y soporte la opción directa para marcar individualmente cada mensaje como leído, además de conservar el control general de marcar todos como leídos.
- **Robustez en envío y sincronización:** Se optimizó `sendUserReply` y `markUserRead` en `SupportReplyManager` para garantizar persistencia local inmediata y sincronización sin bloqueos ante fallos de conexión.

### 2. Notificaciones y Gestión de Historial
- **Marcado selectivo de registros nuevos:** Al abrir la ventana de historial con notificaciones pendientes, únicamente se resaltan los registros recientes/no leídos con el distintivo `NUEVO` y borde cian destacado.
- **Descarte automático al cerrar:** Al cerrar el diálogo de historial, se descartan automáticamente las notificaciones pendientes de historial y se actualiza la marca temporal de lectura, eliminando el contador y la insignia del botón de acceso.

### 3. Internacionalización y Depuración
- Se incorporaron las traducciones correspondientes en portugués para las etiquetas de resolución y marcación de lectura individual.
- Se incrementó el número de compilación interno (`versionCode 906`) y la versión pública (`versionName 1.1.10.190`).
