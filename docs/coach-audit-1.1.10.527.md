# Auditoría Coach 1.1.10.527

- `versionCode`: `1243`.
- `versionName`: `1.1.10.527`.
- Rama de trabajo: `fix-error`.

## Corrección de la comprobación previa

- La ejecución anterior se detuvo en «Comprobaciones previas de catálogos,
  idiomas y planes de prueba» porque `translations_pt.json` no coincidía con
  la salida ordenada de `tools/update-champion-builds.py`.
- Se regeneró únicamente el archivo con la herramienta oficial. Se conservaron
  las traducciones de descarga de evidencia y de informes sin caducidad.
- No se modificó la lógica de captura, adjuntos, permisos de revisión ni
  retención manual; la corrección se limita al artefacto generado que bloqueaba
  el job.

## Verificaciones

- `python3 tools/update-champion-builds.py --check`: correcto.
- `python3 -m json.tool app/src/main/assets/translations_pt.json`: correcto.
- `bash tools/audit-portuguese.sh`: previsto para la ejecución de validación.
- `git diff --check`: previsto antes del push.
- Las pruebas Android locales continúan sin ejecutarse porque este entorno no
  dispone del SDK; la comprobación oficial de Actions es la validación final.
