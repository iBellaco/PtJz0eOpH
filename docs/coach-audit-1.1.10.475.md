# Coach 1.1.10.475 — build 1191

## Referencia de parche 7.3c

- Archivos modificados: `app/build.gradle.kts`, `app/src/main/java/com/example/data/GamePatch.kt`, `.github/scripts/test_patch_content.py` y este registro.
- El parche activo que muestra Coach se corrige de `7.3d` a `7.3c` según la indicación del propietario. No se alteran datos de balance, builds, enfrentamientos ni fuentes históricas: no se comunicaron cambios significativos del juego.
- La versión de Coach sube a `1.1.10.475` (build 1191) para distinguir el APK nuevo y permitir la actualización desde la prueba anterior si la firma instalada coincide.
- La validación de prueba anterior falló en un único fragmento de OCR porque el SDK de Android descargó un ZIP de imagen de emulador ilegible en tres intentos. Las tareas de informe y entrega fallaron en consecuencia; no hubo evidencia de fallo de la lógica de Coach. No se relanza esa compilación obsoleta: la siguiente validación completa comprobará el APK con el parche corregido.

## Verificación

- Pruebas locales de la referencia de parche y del registro de evidencia histórica.
- La compilación ofuscada, firma persistente e instalación del nuevo APK quedan pendientes de Actions.
