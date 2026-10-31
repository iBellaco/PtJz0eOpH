# Coach 1.1.10.401 — build 1117

## Fallo observado

Ejecución 40577861482, revisión 01ba53593742c17f47f345ed8519de53bdf88807,
trabajo installed-spanish 122465723575. Falló
AccountDeletionInstalledTest.portugueseDeletionNeedsBothConfirmationsAndPassword:
`ComposeTimeoutException: Condition still not satisfied after 5000 ms`.
La pila identifica `click` llamado desde `confirmations`. Cuatro de los cinco
casos de borrado aprobaron. El registro anterior no identificaba el testTag
esperado; no se atribuye el timeout a un botón concreto sin evidencia.

El contador corregido en 1.1.10.400 no produjo este fallo: ocurrió antes, en la
prueba de borrado. Aprobaron los trabajos del escáner, las 16 partes de secuencias
nativas y su auditoría, la compilación y los restantes trabajos de validación.
Los fallos finales de publicación/informe dependen de installed-spanish.

## Corrección y prevención

La prueba congela mainClock.autoAdvance al entrar en la confirmación final.
Su click esperaba hasta cinco segundos reales a que existiera un control, pero
solo avanzaba el reloj después de encontrarlo y pulsarlo. Una composición o
ventana pendiente de otro fotograma podía quedarse esperando sin progreso.

- AccountDeletionInstalledTest avanza un fotograma en cada consulta cuando el
  reloj está detenido. Mantiene los cinco segundos, exige un control único y
  visible y espera la confirmación final tras abrirla. No se eliminan aserciones
  de contraseña, dos confirmaciones, cancelación, duplicados o respuesta fallida.
- Los dos casos de confirmación montan deliberadamente el componente después
  de dos fotogramas con el reloj detenido. Esto comprueba el progreso del helper
  sin depender de la carga puntual del emulador. Se mantienen cinco métodos.
- Si vuelve a agotarse la espera, se informa el tag y el estado del reloj y se
  guardan el árbol semántico y una captura, sin sustituir el error original.
- verify-account-deletion-device.py descarga evidencia antes de lanzar las
  aserciones. Un fallo de descarga no oculta el fallo nativo anterior. Las
  ejecuciones repetidas escriben evidencia en carpetas distintas.
- fix-error ejecuta tres veces los cinco casos instalados antes del resto de
  la auditoría española. Se interrumpe al primer fallo; no reintenta para
  convertir un fallo en aprobado. Se añaden los archivos al filtro del flujo.
- test_account_deletion_device.py verifica la conservación de diagnósticos
  usando adb simulado. Ambos flujos ejecutan estas pruebas antes de compilar.
- app/build.gradle.kts incrementa versión y build. No se cambia el escáner ni
  la lógica de borrado productiva; no se realizan operaciones sobre cuentas reales.

## Verificaciones

Tres pruebas Python del recolector y ocho del verificador de títulos aprobadas.
Son pruebas del script con procesos simulados, no pruebas físicas ni ejecuciones
de Android. La comprobación de sincronización Compose debe aprobar en el APK
release instalado por Actions: el entorno local no tiene SDK Android.
Se validan YAML y git diff --check antes del envío. main permanece intacta;
la revisión nueva se envía primero a fix-error y no se da por verde anticipadamente.
