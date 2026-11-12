# Auditoría de Coach 1.1.10.518 (compilación 1234)

## Corrección aplicada

- Se corrigió la tarjeta de avisos del inicio para recibir el idioma activo antes de seleccionar el medio localizado. Esto elimina el error de compilación de Kotlin (`Unresolved reference: currentLanguage`) que bloqueaba la compilación release y las regresiones instaladas.

## Verificación

- Se revisó el log de GitHub Actions de `fix-error` y se confirmó el fallo en `MainDraftingScreen.kt` (líneas 1198 y 1240 del flujo fallido).
- Se ejecutarán las comprobaciones locales disponibles antes del push; la compilación Android local depende del SDK configurado en el entorno.
