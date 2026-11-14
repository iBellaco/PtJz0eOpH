# Auditoría de Coach 1.1.10.547

## Corrección de validación

- Se regeneró `translations_pt.json` con `tools/update-champion-builds.py`
  después de añadir las etiquetas China / Local y Clasificación CN.
- Las traducciones mantienen el orden y el formato generados por la herramienta
  oficial, por lo que la comprobación de catálogos ya no detecta datos obsoletos.

## Verificación

- `python3 tools/update-champion-builds.py --check`: correcto.
- Recursos localizados: correctos.
- Contenido del parche: correcto.
- `git diff --check`: correcto.
