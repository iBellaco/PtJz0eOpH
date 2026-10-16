# Coach 1.1.10.303 — build 1019

## Cambios

- La verificación obligatoria de versión se controla desde la zona de gestión. El ajuste se consulta antes de restaurar una alerta guardada, funciona también antes de iniciar sesión y al desactivarlo libera de inmediato la aplicación y el HUD.
- Cada compra Premium genera una entrada privada con etiqueta `SUSCRIPCION` en reportes y sugerencias. Solo la cuenta administradora puede leerla; no se copia a la bandeja del comprador ni queda disponible para moderación.
- Los reportes guardan `es` o `pt`, detectados a partir del título y el mensaje con el idioma seleccionado como desempate. La vista de gestión muestra la bandera correspondiente. El saludo automático se almacena en español o portugués y las reglas aceptan ambas variantes.
- Las estadísticas publicadas se conservan por campeón y línea. WildRiftFire aporta todas sus filas por rol y WildRiftCore se interpreta dentro del panel `data-role` correspondiente. Al cambiar de línea, victoria, selección y bloqueo usan esa fila; una línea no publicada muestra dato ausente y no reutiliza el porcentaje global o de la línea principal.
- La caché local conserva las tasas por línea y sus cambios entre consultas.

## Archivos principales

- `AppUpdateManager.kt`, `AdminDashboardDialog.kt`, `firestore.rules`.
- `FeedbackRepository.kt`, `SupportLanguageDetector.kt`, `SupportConversationPolicy.kt`, `SupportReportDecoder.kt`, `FeedbackReport.kt`, `AdminFeedbackPanel.kt`, `FeedbackReportCard.kt`, `SupportReportDialog.kt`.
- `EssenceEconomyRepository.kt`, `server/economy/policy.mjs`, `server/economy/service.mjs`.
- `PublishedStatsParser.kt`, `RegionalTierParser.kt`, `GlobalTierSourceClient.kt`, `GlobalTierConsensus.kt`, `BestBuildWrScraper.kt`, `WildRiftRepository.kt`, `Champion.kt`, `ChampionRoleAdapter.kt`.

## Verificaciones

- Suite de economía: 45 pruebas correctas, incluida validación estricta de idioma.
- Regresiones añadidas para detección español/portugués, saludo localizado, tasas distintas por línea y selección de la fila activa.
- Reglas añadidas para lectura pública del interruptor de versión, escritura exclusiva administrativa y avisos de suscripción inaccesibles para comprador y moderación.
- `git diff --check`: correcto.
- La compilación Android local quedó bloqueada porque el entorno no pudo descargar Gradle; se exige completar pruebas Android, reglas, APK firmado/ofuscado e instalación en español y portugués en el flujo de validación antes de integrar.
