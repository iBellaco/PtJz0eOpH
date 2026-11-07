# Coach 1.1.10.491 — Corrección de compilación del informe de coordenadas

## Diagnóstico

- La validación de `fix-error` quedó en rojo en la compilación Kotlin de la
  aplicación y en la compilación release del APK.
- `CalibrationSendButton` intentaba resolver un texto composable dentro del
  callback de clic, un contexto no composable que el compilador rechazó.

## Corrección

- El texto del límite diario se resuelve al entrar en la composición y se
  reutiliza desde el callback y la etiqueta del botón.
- Se conserva el flujo unificado: primero guarda las coordenadas del
  dispositivo y después intenta enviarlas al soporte administrativo.
- Se incrementó la versión de producción a `1.1.10.491`.

## Verificación

- `git diff --check` ejecutado sin errores.
- La compilación Android se debe confirmar en GitHub Actions, que es el entorno
  del proyecto con Android SDK; este entorno local no tiene SDK instalado.

## Alcance de operación

No se añadió monitoreo continuo. Esta corrección solo atiende el error de
compilación detectado en la última ejecución de `fix-error`.
