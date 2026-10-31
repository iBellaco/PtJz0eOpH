# Coach 1.1.10.245 — código 961

## Cambios

- Al quitar Premium, si la solicitud pasa a la cola de procesamiento, la pantalla deja de esperar varios minutos por la confirmación. Cierra el aviso de confirmación, comunica que la solicitud está pendiente y devuelve de inmediato el control del gestor.
- Si el servicio completa el cambio en el momento, se conserva la actualización inmediata de la cuenta. Los errores diferentes a una solicitud pendiente conservan el aviso y permiten volver a intentarlo.
- Se agrega una prueba de pantalla que comprueba que el gestor sigue disponible tras recibir una solicitud pendiente.

## Archivos

- `app/src/main/java/com/example/data/EconomyServiceClient.kt`
- `app/src/main/java/com/example/ui/components/UserDetailManagementDialog.kt`
- `app/src/main/java/com/example/ui/components/UserManagementActions.kt`
- `app/src/test/java/com/example/RuntimeVisibilityTest.kt`
- `app/build.gradle.kts`, esta auditoría.

## Verificación

- Auditoría de idioma y catálogos: 2.595 textos visibles, sin hallazgos de español en portugués.
- Android Lint aprobado y `:app:testDebugUnitTest` aprobó 562 pruebas, 0 fallidas, 0 errores y 0 omitidas.
- Auditoría renderizada portuguesa aprobada. La primera ejecución encontró una aserción de prueba con un encabezado incorrecto; se retiró esa comprobación adicional y la ejecución completa posterior pasó.
- La ejecución de Actions `38023630309` aprobó la compilación release con R8, la verificación de firma y los recorridos instalados en español y portugués.
- La descarga local de Gradle no estuvo disponible; la compilación y todas las comprobaciones de Android se realizaron en Actions.

## Resumen para testers

Coach 1.1.10.245: en el gestor de usuarios, selecciona Quitar Premium y confirma. Comprueba que, si el cambio queda en espera, el aviso se cierre, aparezca el mensaje de solicitud pendiente y los controles del gestor vuelvan a responder sin esperar varios minutos. Comprueba también el caso de respuesta inmediata y que los errores permitan cancelar o reintentar. Repite en español y portugués.
