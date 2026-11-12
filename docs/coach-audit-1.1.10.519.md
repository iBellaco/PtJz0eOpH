# Auditoría de Coach 1.1.10.519 (compilación 1235)

## Cambios aplicados

- El gestor de anuncios y noticias permite elegir publicación General, Español, Português o ambos idiomas y cargar multimedia horizontal opcional por idioma, conservando compatibilidad con avisos anteriores.
- El flujo de moderación pública dejó fuera del anuncio visible el campo interno de esencias cobradas; el dato privado permanece únicamente en la vista de gestión correspondiente.
- Los diagnósticos muestran de forma explícita el nombre de usuario remitente en la tarjeta de administración, con correo solo como respaldo cuando no existe nombre.
- Se añadió una prueba de selección de banner según idioma y se mantuvo el filtrado de los diagnósticos fuera de la bandeja del usuario.

## Verificación

- Se revisó la ejecución roja de `fix-error`: el fallo exacto era `SponsorReviewPolicyTest.publicationUsesRemoteContentAndExcludesPrivateAccountingFields`, porque `chargedEssence` se estaba publicando en el mapa público.
- `npm test` en `server/economy`.
- `python3 .github/scripts/validate-localized-resources.py`.
- `python3 tools/update-champion-builds.py --check`.
- `bash tools/audit-portuguese.sh`.
- `git diff --check`.
- La compilación Android local requiere un Android SDK configurado; el flujo de Actions la ejecuta con su SDK.
