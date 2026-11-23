# Coach 1.1.10.588 (1304) · Informe bilateral y calibración nubia NX809J

## Problema corregido

El informe de coordenadas solo incluía slots con una identidad confirmada.
Cuando el Hub cubría la columna rival, el informe podía parecer incompleto y no
permitía distinguir entre un rival inspeccionado sin evidencia y un lado que no
se hubiera procesado.

## Cambio realizado

- El informe enumera siempre los cinco slots aliados y los cinco rivales.
  Identidades confirmadas conservan su fuente; los slots sin evidencia aparecen
  explícitamente como `N/D`.
- El informe enumera los cinco baneos de cada lado y conserva su estado. Un
  candidato no confirmado se marca como candidato y no como identidad válida.
- El resumen de la captura separa cuántas identidades pertenecen a Aliado y a
  Rival, incluso cuando el Hub oculta parte de la imagen.
- Se registró la calibración exacta de `nubia NX809J`, Android 16/API 36,
  pantalla 2688×1216 y captura alineada 1920×869. El perfil permanece separado
  de `nubia NX789J` aunque sus dimensiones coincidan.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/service/screen/DraftVisionScanner.kt`
- `app/src/main/java/com/example/service/screen/MeasuredDeviceCalibrationDefaults.kt`
- `app/src/main/java/com/example/ui/components/CalibrationControls.kt`
- `app/src/test/java/com/example/CalibrationEvidenceImageTest.kt`
- `app/src/test/java/com/example/DeviceDraftProfileTest.kt`
- `app/src/test/java/com/example/DraftMultiDeviceAuditTest.kt`
- `docs/scanner-calibration-backup.json`
- `docs/device-compatibility.md`
- `docs/CONTINUIDAD_COORDENADAS_MULTIDISPOSITIVO.md`
- `docs/draft-recognition-contract.md`

## Verificaciones

- Se añadieron pruebas para el perfil exacto y su alias de captura.
- Se añadió una regresión que exige la presencia explícita de ambos lados en el
  informe cuando la columna rival no tiene identidades confirmadas.
- `jq empty docs/scanner-calibration-backup.json` y `git diff --check` pasan.
- Las suites dirigidas del escáner pasaron previamente en el entorno local con
  34 pruebas. La repetición posterior quedó bloqueada antes de compilar porque
  el worker local actual no tiene un Android SDK configurado; la misma
  validación queda lista para ejecutarse en el entorno reproducible de CI.
