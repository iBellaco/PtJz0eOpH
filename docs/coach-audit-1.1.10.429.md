# Auditoría Coach 1.1.10.429

## Cambios
- Android 16: arranque de captura con tipo de servicio mediaProjection independiente de specialUse; manejo de rechazo al lanzar el servicio y al pedir autorización.
- La pantalla deja de marcar la superposición como activa antes de que Android confirme la existencia del servicio; se corrige el VirtualDisplay nulo.
- Interruptor administrativo independiente para deshabilitar la activación en Inicio, con estado público de solo lectura sin exponer los identificadores del visor y sincronización reactiva.
- Cuando se deshabilita el botón, se detiene la superposición activa, se cancela una solicitud de inicio pendiente y no se aceptan pulsaciones.
- Textos nuevos traducidos a español y portugués, con test de independencia del visor.

## Versión
- versionCode: 1145
- versionName: 1.1.10.429

## Verificaciones
- Revisiones estáticas de reemplazos únicos, tipos de servicio, permisos y contrato de políticas.
- Pendiente de ejecutar GitHub Actions, compilar release y validar en dispositivo físico Red Magic 10 Pro Android 16.
