# Coach 1.1.10.464 — build 1180

## Corrección de validación OCR

Se amplió únicamente el presupuesto de la prueba nativa para capturas de hasta
960 píxeles de ancho. Estas geometrías requieren más tiempo durante la cadena
de recuperación de títulos en el emulador de validación. Las demás resoluciones
mantienen el límite anterior para no aumentar el tiempo de la matriz completa.

## Verificación

- El fallo quedó localizado en la geometría `854×480`.
- Las otras comprobaciones del flujo terminaron correctamente.
- El cambio se limita al artefacto de secuencia OCR afectado.
