# Coach 1.1.10.586 (1302) · Evitar la cancelación de la auditoría multidispositivo

## Problema corregido

La ejecución completa se cancelaba cuando `scanner-tests` alcanzaba el límite
de 60 minutos durante la auditoría de posiciones y baneos en los perfiles de
captura. Las comprobaciones anteriores ya habían terminado correctamente, pero
la cancelación impedía publicar el resultado global.

## Cambio realizado

- Se amplió únicamente el presupuesto de tiempo del job de auditoría del
  escáner de 60 a 90 minutos.
- Se conserva la misma matriz, los mismos tests, el orden de las comprobaciones
  y la condición que exige que todas terminen correctamente.
- No se relajan errores ni se marca como válida una auditoría incompleta.

## Archivos modificados

- `.github/workflows/build-apk.yml`
- `app/build.gradle.kts`

## Verificaciones

- Se comprobó que el job cancelado agotó exactamente el límite anterior durante
  la auditoría multidispositivo.
- Se verificará la sintaxis YAML y la validación selectiva antes de pasar esta
  revisión a la validación completa.
