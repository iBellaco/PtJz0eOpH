# Coach 1.1.10.316 (build 1032)

## Correcciones del Hub y del Draft

- Se retiró la pestaña Historial del Hub flotante para dejar el espacio de navegación a Draft, Tiers y Campeones.
- Cambiar de pestaña limpia el detalle de campeón abierto; la build ya no tapa ni se conserva al navegar por el Hub.
- Campeones usa el espacio restante del panel mediante una lista con tamaño acotado, evitando que filtros y contenido se recorten entre sí.
- Al completar los diez campeones del Draft, el Hub abre automáticamente la build del campeón aliado correspondiente a la línea activa.

## Validación

- `git diff --check` ejecutado correctamente.
- La suite Android y la auditoría del APK quedan delegadas al workflow de GitHub Actions por falta de descarga de Gradle en el entorno local.
