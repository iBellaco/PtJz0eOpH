# Coach 1.1.10.577 (1293) · Estabiliza las filas medidas y las filas adaptativas de baneos

## Problema resuelto

- La validación remota volvió a ejecutar las pruebas después del límite temporal
  de Maven y encontró tres regresiones en `BannedPortraitScannerTest`.
- Una fila medida del Samsung SM-S938B se estaba tratando como adaptativa y sus
  centros se desplazaban junto con la observación.
- Las filas adaptativas que se copiaban temporalmente entre fotogramas se
  convertían accidentalmente en geometría fija; por eso un hueco de baneo podía
  quedar como `PENDING` en vez de confirmarse como vacío.

## Corrección

- Se añadió una marca interna para distinguir perfiles físicos medidos de copias
  transitorias de seguimiento.
- Los perfiles medidos conservan sus coordenadas aunque el informe indique
  `Manual=false`.
- Las filas adaptativas continúan ajustándose a la evidencia del viewport sin
  bloquearse por diferencias temporales de coordenadas.
- La marca se conserva al guardar y recuperar una calibración de captura.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/service/screen/MeasuredDeviceCalibrationDefaults.kt`
- `app/src/main/java/com/example/service/screen/VisionCalibrationConfig.kt`
- `app/src/test/java/com/example/DeviceDraftProfileTest.kt`

## Verificación

- El run remoto reprodujo tres aserciones concretas antes del arreglo.
- `git diff --check` se ejecuta respetando el CRLF existente del archivo Gradle.
- La prueba local dirigida no pudo iniciar porque este entorno no permite
  descargar Gradle 9.3.1; la validación remota queda encargada de compilar y
  ejecutar la suite con su caché de dependencias.
