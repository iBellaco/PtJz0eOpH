# Auditoría de Coach 1.1.10.524 (compilación 1240)

## Corrección de la ejecución roja

- La validación selectiva de la versión anterior falló en el único test
  `CoachRoutineRegressionTest.title recovery stays fast before a pick and is less frequent after evidence`.
- La implementación activa correctamente la recuperación completa en la lectura
  16; la expectativa del test había quedado invertida y esperaba una pasada rápida.
- Se corrigió únicamente esa expectativa y se añadieron comprobaciones para las
  lecturas inmediatamente anterior y posterior a cada recuperación periódica.

## Alcance conservado

- No se modificó la lógica de precarga, la cadencia de producción ni el perfil
  medido del Xiaomi 25062RN2DL; la corrección afecta solo al contrato de prueba.
- Se mantiene la precarga sin llamadas composables desde la capa de datos y la
  reducción de recuperaciones OCR pesadas implementadas en 1.1.10.523.

## Verificaciones

- Se revisó el job y el log exacto de la ejecución roja.
- `python3 tools/update-champion-builds.py --check`
- `python3 .github/scripts/validate-localized-resources.py`
- `bash tools/audit-portuguese.sh`
- `node --check server/economy/policy.mjs`
- `node --check server/economy/service.mjs`
- `python3 -m json.tool app/src/main/assets/translations_pt.json`
- `python3 -m json.tool docs/scanner-calibration-backup.json`
- `npm test --prefix server/economy`
- `git diff --check`
- La prueba Android se delega al flujo oficial porque el entorno local no tiene
  Android SDK configurado; el fallo anterior fue una aserción, no una compilación.

