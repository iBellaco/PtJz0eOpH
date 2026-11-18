# Coach 1.1.10.572 (1288) · Corrige la prueba dirigida del escáner

## Corrección

- La validación anterior llegó a compilar la aplicación y falló al compilar `DraftMultideviceBoxesTest` por usar una firma genérica que Robolectric no admite en `RuntimeEnvironment.getApplication`.
- La prueba ahora usa la firma compatible con la versión de Robolectric del proyecto.

## Verificación

- Pasaron localmente `DraftMultideviceBoxesTest` y `BannedPortraitScannerTest`.
- La ejecución local terminó con `BUILD SUCCESSFUL`.
- La versión de producción se incrementó a 1.1.10.572 (1288).
- La nueva ejecución remota debe confirmar la validación completa y la compilación del APK.
