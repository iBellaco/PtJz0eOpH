# Auditoría Coach 1.1.10.525

- `versionCode`: `1241`.
- `versionName`: `1.1.10.525`.
- Rama de trabajo: `fix-error`.

## Cambios funcionales

- La verificación de actualización mantiene un interruptor global, pero ya no bloquea por el solo hecho de existir una publicación más nueva. El administrador define `minimumSupportedVersionCode`; únicamente una instalación con un código inferior queda obsoleta y se bloquea cuando existe una publicación que alcanza ese umbral. Un valor `0` desactiva la obsolescencia y evita dejar al usuario apuntando a una versión que todavía no existe.
- El panel de administración permite consultar y guardar el código mínimo obligatorio. Las cadenas existentes de español y portugués describen la política sin exponer configuración de infraestructura.
- Se registró la calibración aportada para `nubia NX809J` (2688x1216, Android 16/API 36, densidad 3.25, escala de fuente 1.0) en `MeasuredDeviceCalibrationDefaults` y en `docs/scanner-calibration-backup.json`. El diagnóstico de ese equipo informó una captura real de 1920x869; se añadió un alias exacto para ese tamaño con los mismos ratios normalizados, limitado al mismo fabricante y modelo. No se comparte la geometría con otros teléfonos por resolución.
- Los informes de coordenadas y diagnóstico incluyen evidencia compacta de los campeones confirmados: lado, posición, identidad confirmada y fuente (`texto`/`imagen`), además de los baneos reconocidos por imagen, estado y similitud. No se adjuntan capturas, correos ni credenciales.

## Lectura del diagnóstico recibido

El equipo `nubia NX809J` registró una sesión de 2373 s (39,6 min), 551/551 fotogramas procesados, 19 omitidos y un promedio de procesamiento de 241 ms (máximo 4920 ms). La batería pasó de 73% a 58% y de 3901 a 3114 mAh: 787 mAh observados durante la sesión (aprox. 1191 mAh/h). La potencia instantánea de 14,54 W es alta para una medición puntual, aunque la temperatura de batería (38 °C), el estado térmico Android (0) y la memoria disponible (5556 MiB) no indican estrangulamiento. Estos datos representan el dispositivo completo, no un consumo aislado de Coach.

La evidencia de detección se mantiene separada del diagnóstico de rendimiento para que un campeón confirmado por OCR no se presente como reconocimiento visual. Si no hay una fuente confirmada, el informe lo marca como `fuente=N/D` en lugar de inventar certeza.

## Comprobaciones previstas

- Validación de JSON de traducciones y respaldo de calibración.
- Pruebas unitarias de política de obsolescencia, sobre de coordenadas y perfiles exactos (incluido el alias 1920x869 observado).
- Auditoría de recursos localizados, catálogos y scripts de servidor.
- `git diff --check` y, si el entorno dispone del SDK, la tarea de pruebas Android; de lo contrario, el flujo oficial de Actions queda como verificación final.
