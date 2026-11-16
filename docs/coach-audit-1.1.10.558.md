# Coach 1.1.10.558 — catálogo portugués regenerado

Versión: 1.1.10.558; versionCode: 1274.

## Corrección

- Se regeneró `translations_pt.json` mediante `tools/update-champion-builds.py`
  para que las nuevas frases China/Elo queden en la posición y formato que
  exige el catálogo generado.
- Se conserva la traducción portuguesa del selector China, sus segmentos Elo y
  la explicación del origen Tencent.

## Validación

- `python3 tools/update-champion-builds.py --check`: correcto.
- `bash tools/audit-portuguese.sh`: cero hallazgos de texto español visible.
- `python3 .github/scripts/validate-localized-resources.py`: correcto.

## Texto para pruebas

Coach 1.1.10.558

- Comprueba el selector China por Elo en portugués.
- Confirma que el catálogo portugués carga sin advertencias y que el monitoreo
  conserva Global y China.
