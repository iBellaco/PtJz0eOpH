# Coach 1.1.10.590 (1306) · Matriz ampliada del resumen del escáner

## Problema corregido

La matriz de dispositivos incorporó el perfil `NX809J`, pero el resumen aún
esperaba seis perfiles de dispositivo. La auditoría generaba 495 casos válidos
con tres variantes por perfil, mientras el resumen exigía 492 y marcaba la
validación en rojo.

## Cambio realizado

- Se actualizó el resumen para esperar siete perfiles de dispositivo y 495
  casos simulados.
- Se actualizaron las aserciones de la prueba del resumen para cubrir la matriz
  ampliada.
- Se incrementó la versión a `1.1.10.590` (build `1306`).

## Verificaciones

- La prueba del resumen del escáner cubre 165 perfiles y 495 casos.
- Se ejecutarán las comprobaciones de scripts y herramientas antes de integrar
  nuevamente esta corrección en la validación completa.
