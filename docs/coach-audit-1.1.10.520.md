# Auditoría de Coach 1.1.10.520 (compilación 1236)

## Cambios aplicados

- El botón de diagnóstico del Hub aparece después de 60 segundos continuos con Auto-Scan activo, evitando informes demasiado breves y manteniendo el límite de cinco envíos diarios.
- El diagnóstico registra el tiempo total de uso de Coach desde el inicio de la sesión del overlay, además de la duración puntual de la medición, consumo observado de batería, carga, energía, corriente, voltaje y potencia.
- El feedback de diagnóstico incluye el estado del draft guardado: rol, primer pick, modo de partida, selecciones aliadas y rivales, baneos detectados, completitud, Auto-Scan y la recomendación principal con su porcentaje estimado.
- Se regeneraron los recursos generados de campeones y traducciones para corregir el fallo rojo de `fix-error` por `translations_pt.json` desactualizado.

## Archivos principales

- `app/build.gradle.kts`
- `app/src/main/java/com/example/data/DeviceDiagnosticTelemetry.kt`
- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/main/java/com/example/ui/components/DeviceDiagnosticButton.kt`
- `app/src/main/assets/translations_pt.json`

## Verificación

- `python3 tools/update-champion-builds.py --check`.
- `python3 .github/scripts/validate-localized-resources.py`.
- `bash tools/audit-portuguese.sh`.
- `npm test --prefix server/economy`.
- `node --check server/economy/policy.mjs server/economy/service.mjs`.
- `python3 -m json.tool app/src/main/assets/translations_pt.json`.
- `git diff --check`.
- La compilación Android local no pudo completarse porque el entorno no dispone de un SDK/daemon utilizable; la validación Android queda para GitHub Actions.

