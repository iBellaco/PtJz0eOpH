# Auditoría de Coach 1.1.10.516

## Corrección

- Se regeneró `app/src/main/assets/translations_pt.json` con `tools/update-champion-builds.py` para que las comprobaciones de datos generados coincidan con el catálogo actual.
- La corrección atiende el fallo del job de validación que terminaba con `AssertionError: Outdated generated data: app/src/main/assets/translations_pt.json`.
- La ejecución antigua que permanecía en estado `queued` corresponde a un SHA anterior; no se canceló.

## Verificación

- `python3 tools/update-champion-builds.py --check`.
- `python3 .github/scripts/validate-localized-resources.py`.
- `bash tools/audit-portuguese.sh`.
- `git diff --check`.
- La compilación y las pruebas dirigidas de la versión anterior ya habían terminado correctamente; el siguiente flujo debe validar esta regeneración junto con el incremento de versión.
