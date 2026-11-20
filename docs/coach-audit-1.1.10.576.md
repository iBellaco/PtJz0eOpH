# Coach 1.1.10.576 (1292) · Separa el décimo pick de los baneos en Samsung

## Correcciones implementadas

- Se añadió el perfil exacto del Samsung SM-S938B para `2340x1080` y sus alias
  de captura `1920x886` y `1280x590`.
- Se respetan las filas de baneos medidas aunque el informe indique
  `Manual=false`; solo los cambios guardados por el usuario se consideran
  ajustes manuales.
- Se añadió una exclusión contextual del quinto círculo superior del lado del
  décimo pick para que no entre en la fila de baneos ni desplace el quinto ban.
- Una identidad que ya fue confirmada como selección se elimina de las
  observaciones de baneos antes de actualizar el visor.
- Se conservaron el orden y el desplazamiento individual de los slots aliados
  medidos en la captura de Samsung.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/main/java/com/example/service/screen/BanDetection.kt`
- `app/src/main/java/com/example/service/screen/BannedPortraitScanner.kt`
- `app/src/main/java/com/example/service/screen/DraftBanWorker.kt`
- `app/src/main/java/com/example/service/screen/DraftVisionScanner.kt`
- `app/src/main/java/com/example/service/screen/LiteRTVisionClassifier.kt`
- `app/src/main/java/com/example/service/screen/MeasuredDeviceCalibrationDefaults.kt`
- `app/src/main/java/com/example/service/screen/VisionCalibrationConfig.kt`
- `app/src/test/java/com/example/BannedPortraitScannerTest.kt`
- `app/src/test/java/com/example/DeviceDraftProfileTest.kt`
- `docs/samsung-sm-s938b-vision-draft-calibration.md`

## Verificación

- Se añadieron pruebas del alcance exacto del perfil Samsung, de las filas
  medidas no manuales y de la exclusión del círculo del décimo pick.
- La comprobación de diferencias terminó sin errores usando la configuración
  de fin de línea CRLF existente en `app/build.gradle.kts`.
- Se intentó ejecutar la suite dirigida del escáner, pero el wrapper no pudo
  descargar Gradle 9.3.1 porque la conexión de red del entorno fue rechazada;
  por ello la compilación y las pruebas automatizadas quedan pendientes de la
  validación remota.
- La captura binaria suministrada en la conversación no está disponible como
  archivo local; se guardó su evidencia textual y su hash sin fabricar un
  fixture.
