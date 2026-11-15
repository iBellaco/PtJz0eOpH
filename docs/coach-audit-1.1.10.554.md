# Coach 1.1.10.554 — Referencia exacta para nubia NX789J y anillos superiores estables

Versión: 1.1.10.554; versionCode: 1270.

## Cambios

- Se registró la calibración aportada para `nubia NX789J`, Android 16/API 36,
  pantalla 2688×1216, con un alias para la captura 1920×869 observada en el
  informe. El perfil solo coincide con ese fabricante, modelo y dimensiones;
  no se comparte por resolución con otros dispositivos.
- Se conservaron exactamente los ratios de slots, baneos y barra superior del
  informe de Diego, incluidos los cinco baneos de cada lado y sus diámetros.
- Los cinco círculos blancos superiores de la evidencia se dibujan con centros
  redondeados a píxel entero entre sus extremos guardados. Esto elimina la
  variación visual causada por fracciones de píxel sin modificar la geometría
  normalizada usada por el escáner.
- El respaldo JSON y la documentación de compatibilidad incluyen el perfil y
  su alias de captura.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/service/screen/MeasuredDeviceCalibrationDefaults.kt`
- `app/src/main/java/com/example/service/screen/CalibrationEvidenceImage.kt`
- `app/src/test/java/com/example/DeviceDraftProfileTest.kt`
- `app/src/test/java/com/example/CalibrationEvidenceImageTest.kt`
- `docs/scanner-calibration-backup.json`
- `docs/draft-recognition-contract.md`
- `docs/device-compatibility.md`

## Verificaciones

- Se añadieron regresiones para el perfil exacto, el alias 1920×869 y el
  espaciado entero de los anillos superiores.
- `git diff --check` y validación JSON pendientes de ejecutar junto con las
  pruebas dirigidas del escáner.
- Debe repetirse la prueba física en el dispositivo para confirmar que el
  pequeño desfase visual de los círculos blancos desapareció en la pantalla
  real.

## Texto para pruebas

Coach 1.1.10.554

- Coach reconoce la referencia exacta de nubia NX789J en 2688×1216 y en su
  captura 1920×869.
- Los ratios de slots y baneos se conservan separados de otros dispositivos.
- Los círculos blancos superiores de la evidencia mantienen sus extremos y
  ahora se dibujan con separación estable a nivel de píxel.
- La detección adaptativa continúa activa fuera de este perfil exacto.
