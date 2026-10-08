# Operaciones de esencias de Coach

La función `coachEconomy` concentra compras, solicitudes y resoluciones de canje, ajustes, tiempo Premium, suscripciones a creadores y envíos de anuncios. No se transmiten criptomonedas ni se almacenan claves de billeteras: el operador confirma un pago realizado por otro medio. Debe verificar destino, importe y evidencia del pago antes de marcarlo pagado. Un rechazo devuelve el débito completo una sola vez.

## Activación y permisos

1. Mantener la firma de Coach y los Secrets actuales. No copiar claves al proyecto, al ZIP ni a los registros.
2. Ejecutar las pruebas del servicio y `tests/firestore` en el proyecto aislado. La conexión de producción no se utiliza en esos tests.
3. Ejecutar **Support and streamer permissions**, indicando la rama validada. Solo en la primera migración, introducir en `bootstrap_email` el correo del titular existente: se comprueba que esté verificado, habilitado y ya tenga el perfil administrativo protegido. No se conceden permisos por un marco ni se convierten automáticamente los perfiles que dicen ser administradores. Se conservan los claims existentes.
4. El flujo requiere una identidad de servidor que pueda administrar usuarios, desplegar la función y publicar reglas. Un token de CLI sin acceso de servidor no basta. Cloud Functions exige facturación habilitada y los permisos de compilación, ejecución y despliegue correspondientes. Si falta alguno, se detiene antes de publicar las reglas nuevas.
5. Verificar que el endpoint existe y rechaza solicitudes sin sesión; leer de vuelta las reglas publicadas. Abrir de nuevo Coach o renovar la sesión actualiza el claim del titular.

La existencia del código, un resultado de emulator o un workflow configurado no demuestran que el servicio esté activo. Registrar el resultado real del despliegue en la auditoría de entrega. Ante un fallo, conservar las reglas anteriores y corregir la conexión; nunca introducir un débito local como alternativa. Las versiones antiguas que escribían saldos directamente dejan de hacerlo cuando se activan las reglas nuevas.

## Comprobaciones operativas

- Confirmar el saldo, el importe y la comisión mostrada. Una comisión que cambia antes de enviar provoca una nueva confirmación; no se debita silenciosamente un importe distinto.
- Repetir una petición con su mismo identificador y comprobar un solo comprobante. El mismo ID con otro contenido se rechaza.
- Intentar cobrar desde una sesión sin permisos, suspendida o pendiente de eliminación. No debe aparecer un movimiento.
- Rechazar dos veces un canje: un único reembolso. Una solicitud resuelta no cambia de pagada a rechazada ni viceversa.
- Revisar español y portugués, pagos a creadores y anuncios. Un error de conexión no se anuncia como éxito ni concede una suscripción local.

## Revisión especializada pendiente

Las políticas públicas y las de la app explican captura local, servicios de cuenta, proveedores, soporte y eliminación. Antes de ofrecer canjes o monetización al público, un especialista debe revisar: titular y jurisdicción aplicable; tratamiento y conservación de destinos de pago; prevención de fraude, edades permitidas y obligaciones fiscales; condiciones de los proveedores de USDT/Binance; reglas de Google Play para bienes digitales; derechos de Riot y de las fuentes consultadas. El código no acredita esa revisión ni convierte las esencias en un producto financiero autorizado. Registrar la decisión de habilitar o limitar esas funciones y actualizar las condiciones a esa decisión.
