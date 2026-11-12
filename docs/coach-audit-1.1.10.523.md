# Auditoría de Coach 1.1.10.523 (compilación 1239)

## Corrección de la ejecución roja

- La ejecución anterior falló durante `compileDebugKotlin` y `compileReleaseKotlin` en `AppNoticeManager.kt:690:59`.
- La causa exacta era invocar `currentAppLanguage()`, una función `@Composable`, desde la precarga no composable de anuncios.
- La precarga ahora lee el estado estable de `AppLanguage.current`, conservando la selección del idioma activo sin iniciar una composición ni descargar las dos variantes.

## Rendimiento del escáner

El diagnóstico del Xiaomi 25062RN2DL mostró 50 fotogramas recibidos, 48 procesados,
42 omitidos, 0,25 FPS, 3.264 ms de procesamiento medio y un máximo de 43.862 ms.
La sesión consumió 2% de batería en 475 s (139 mAh medidos, 3,63 W instantáneos)
con 37,7 °C y sin estado térmico adverso. El cuello de botella es el OCR de
recuperación, no un sobrecalentamiento de la batería.

- La recuperación completa de títulos deja de ejecutarse cada ocho lecturas.
- Antes de la primera selección confirmada se mantiene la ruta rápida, porque no
  hay evidencia nueva que justifique una cadena OCR pesada.
- Tras detectar selecciones, la recuperación completa sigue disponible cada 16
  lecturas para conservar la tolerancia a nombres difíciles sin bloquear el
  escáner en equipos con tiempos de OCR elevados.
- Se añadieron pruebas de regresión para estos límites; no se relajan las
  comprobaciones de identidad ni se descartan selecciones confirmadas.

## Perfil de calibración respaldado

Se registró el perfil medido de `Xiaomi 25062RN2DL` para captura `2340x1080`.
La aplicación lo usa únicamente cuando coinciden fabricante, modelo y
dimensiones exactas y no existe una calibración guardada por el usuario. Las
medidas también quedan en `docs/scanner-calibration-backup.json` para respaldo.

## Verificaciones

- `python3 tools/update-champion-builds.py --check`
- `python3 .github/scripts/validate-localized-resources.py`
- `bash tools/audit-portuguese.sh`
- `node --check server/economy/policy.mjs`
- `node --check server/economy/service.mjs`
- `python3 -m json.tool app/src/main/assets/translations_pt.json`
- `python3 -m json.tool docs/scanner-calibration-backup.json`
- `npm test --prefix server/economy`
- `git diff --check`
- La compilación Android local se delega a la validación porque este entorno no
  tiene un Android SDK configurado.

