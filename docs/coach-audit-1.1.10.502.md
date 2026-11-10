# Auditoría de localización del filtro de favoritos del Hub de Coach 1.1.10.502

## Versión

- `versionName`: `1.1.10.502`
- `versionCode`: `1218`
- Alcance: corrección de textos del filtro de recomendaciones del Hub para portugués.

## Corrección aplicada

- Los textos nuevos del filtro de favoritos dejaron de estar incrustados en Kotlin y ahora usan recursos de español y portugués.
- Se mantienen el mínimo de cinco favoritos por línea, la aplicación al primer pick y el cambio activado/desactivado.

## Verificaciones

- `validate-localized-resources.py`: correcto.
- `audit-portuguese.sh`: correcto, sin literales en español detectados.
- La ejecución roja se debió exclusivamente a la auditoría de portugués; no reportó fallo de compilación ni de lógica del filtro.
