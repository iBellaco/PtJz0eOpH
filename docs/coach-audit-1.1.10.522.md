# Auditoría Coach 1.1.10.522

## Correcciones

- Las campañas patrocinadas admiten medios independientes por idioma: banner horizontal y medio vertical para Español y para Português. Las campañas de un solo idioma mantienen el flujo de un único medio y las campañas bilingües conservan ambas variantes al sincronizarse y mostrarse.
- El gestor de avisos conserva las variantes verticales por idioma al editar, guardar o volver a cargar una publicación.
- La visibilidad de cada anuncio y la vista ampliada seleccionan el medio del idioma activo, con compatibilidad para anuncios antiguos que solo tienen un medio general.
- El cálculo y la validación de patrocinio contemplan las seis ranuras multimedia y rechazan una campaña bilingüe con una sola variante vertical incompleta.
- El botón de diagnóstico del overlay queda disponible después de cumplir la ventana mínima de Auto-Scan aunque el draft termine y Auto-Scan se apague; se restablece al cerrar el overlay.
- La precarga de avisos usa únicamente el banner del idioma activo para limitar descargas y consumo de batería.

## Versión

- `versionCode`: 1238
- `versionName`: `1.1.10.522`

## Verificaciones

- `python3 tools/update-champion-builds.py --check`
- `python3 .github/scripts/validate-localized-resources.py`
- `bash tools/audit-portuguese.sh`
- `node --check server/economy/policy.mjs`
- `node --check server/economy/service.mjs`
- `python3 -m json.tool app/src/main/assets/translations_pt.json`
- `npm test --prefix server/economy` (52 pruebas)
- `git diff --check`
- La prueba nativa de Gradle no pudo ejecutarse localmente porque este entorno no tiene Android SDK configurado; queda delegada al flujo de validación.
