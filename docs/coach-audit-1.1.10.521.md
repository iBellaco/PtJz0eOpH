# Auditoría de Coach 1.1.10.521 (compilación 1237)

## Corrección aplicada

- La validación completa falló en `TierCategoriesRenderedTest.allFiveCategoriesAreReachableAndThereIsNoExportButton` porque sus campeones de prueba tenían tiers, pero `statsCheckedAt = 0`; el filtro de producción los descartaba correctamente como campeones sin datos competitivos.
- Se actualizó únicamente el fixture de la prueba para marcar estadísticas competitivas válidas. El caso de Sylas conserva explícitamente `hasRegionalStats = false` y sigue verificando que no aparezca en la Tier List.

## Verificación

- Se revisó el job `core-tests` de la ejecución roja y su anotación exacta.
- `python3 tools/update-champion-builds.py --check`.
- `python3 .github/scripts/validate-localized-resources.py`.
- `bash tools/audit-portuguese.sh`.
- `npm test --prefix server/economy`.
- `node --check server/economy/policy.mjs server/economy/service.mjs`.
- `python3 -m json.tool app/src/main/assets/translations_pt.json`.
- `git diff --check`.
- La compilación Android local requiere un SDK que no está disponible en este entorno; se delega a Actions.

