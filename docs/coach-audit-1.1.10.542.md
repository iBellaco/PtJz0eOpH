# Auditoría técnica de Coach 1.1.10.542

## Alcance

Esta versión deja el escáner listo para validar la geometría
multidispositivo sin perfiles de coordenadas medidos precargados y corrige la
regresión de capturas casi cuadradas con viewport interno.

## Cambios aplicados

- `MeasuredDeviceCalibrationDefaults.kt`
  - Eliminó todos los perfiles de dispositivos y sus coordenadas compiladas.
  - Mantiene la API de consulta devolviendo `null`, por lo que la selección
    depende de la geometría adaptativa y de calibraciones guardadas
    explícitamente durante la ejecución.
- `docs/scanner-calibration-backup.json`
  - Se vació el respaldo de mediciones; no contiene perfiles activos.
- `docs/CONTINUIDAD_COORDENADAS_MULTIDISPOSITIVO.md`
  - Se actualizó para documentar el estado sin presets y las reglas del motor.
- `docs/scanner-device-validation.md`
  - Se corrigió la descripción del respaldo para no presentar una medición como
    valor inicial productivo.
- `DeviceDraftProfileTest.kt`
  - Verifica que no exista un valor medido para ningún modelo o dimensión
    previamente aportados.
- `DraftVisionScanner.kt`
  - Cuando la captura contiene barras exteriores y se identifica un viewport
    interno, evita volver a escalar la geometría por la relación de aspecto del
    contenedor. Así los recortes se calculan con el viewport real y no se
    desplaza una fila al iniciar la alineación visual.

## Regresión corregida

La validación selectiva anterior fallaba en la captura cuadrada reducida:
el segundo slot aliado quedaba desplazado aproximadamente 32 píxeles de
referencia y el clasificador devolvía candidatos incorrectos. La causa era
aplicar la adaptación de la relación de aspecto exterior antes de mapear el
viewport letterboxed. La nueva ruta conserva la geometría base para ese caso y
la proyecta una sola vez dentro del viewport observado.

## Verificaciones

- `DraftMultiDeviceAuditTest.formerlyFailingCentersStayAlignedWhenPortraitsChange`
  - BUILD SUCCESSFUL.
- Se validarán nuevamente las suites completas del escáner y los recursos
  localizados antes de pasar la revisión a la siguiente rama.
- JSON de respaldo validado con `python3 -m json.tool`.
- Se incrementó `versionCode` de 1257 a 1258 y `versionName` a `1.1.10.542`.
