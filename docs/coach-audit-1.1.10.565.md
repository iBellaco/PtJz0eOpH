# Coach 1.1.10.565 — arranque estable de la auditoría instalada

## Diagnóstico

- La validación completa de `coach-validacion` falló únicamente en la auditoría instalada en portugués.
- `installed-audit` capturó el launcher de Android (`com.google.android.apps.nexuslauncher`) después de que el comando de actividad devolviera el control; no se había renderizado Coach.
- El APK, la compilación, los tests de visión, la auditoría renderizada y el recorrido instalado en español terminaron correctamente.

## Corrección implementada

- La auditoría resuelve ahora la actividad de Coach mediante el intent `MAIN`/`LAUNCHER`, confirma que existe contenido visible de Coach y reintenta con el launcher del paquete cuando Android mantiene el launcher enfocado.
- Si el arranque vuelve a fallar, conserva jerarquía, captura y logcat para distinguir un problema de emulador de un fallo real de la aplicación.
- Se añadió cobertura unitaria para no aceptar la jerarquía del launcher ni una raíz de Coach todavía vacía como una pantalla válida.
- Se conservan los artefactos `installed-audit-apks` ya generados; la corrección solo afecta la etapa de auditoría instalada.

## Versión

- `versionCode`: 1281
- `versionName`: `1.1.10.565`

## Verificaciones locales

- `python3 -m unittest discover -s tools -p test_portuguese_device_audit.py`
- `git diff --check`
- Sintaxis YAML de todos los workflows en `.github/workflows/`.
