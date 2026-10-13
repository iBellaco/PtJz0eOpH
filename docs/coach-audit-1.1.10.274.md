# Coach 1.1.10.274 (build 990)

## Cambios

- Información legal: retirados el bloque y el acceso de contacto de privacidad en español y portugués.
- Creador de builds: ocultos los componentes básicos y de nivel medio; soporte puede escoger un básico como máximo.
- Resumen de datos: cifras principales visibles, categorías compactas desplegables y detalles de almacenamiento bajo demanda.
- Moderación: rol secundario y verificación directos, con eventos inmutables ligados a cada cambio de cuenta o conversación. La vista de actividad muestra el historial y los contadores de respuestas de soporte, rangos secundarios y verificaciones por moderador.
- Bandeja de entrada: categoría de verificación con tres imágenes obligatorias de hasta 1 MB de origen cada una. La revisión muestra las imágenes y cierra la solicitud junto al cambio de perfil en una sola transacción.
- Reglas de acceso: los cambios de moderación exigen un registro coincidente y la verificación exige tres imágenes con tamaño acotado.

## Archivos

`app/build.gradle.kts`, `firestore.rules`, `tests/firestore/rules.test.mjs`, `app/src/test/java/com/example/AccountDeletionRenderedTest.kt`, `app/src/main/assets/translations_ui_pt.json`, `app/src/main/java/com/example/data/{FeedbackRepository,ModeratorActivityRepository,SupportConversationPolicy,SupportReplyManager}.kt`, `app/src/main/java/com/example/ui/components/{AdminDashboardDialog,AdminDatabaseConsumptionDialog,AdminFeedbackPanel,AdminModeratorActivityDialog,ChampionBuildCreatorDialog,FeedbackReportCard,ModeratorDashboardDialog,PrivacyPolicyDialog,SupportReportDialog,UserDetailManagementDialog}.kt`, `app/src/main/java/com/example/util/{ImageUtils,PrivacyContact}.kt`, `app/src/main/res/values/account_deletion.xml`, `app/src/main/res/values-pt/account_deletion.xml`.

## Validación

- Reglas de seguridad: 60 escenarios existentes y nuevos ejecutados en el emulador, incluyendo consulta global de actividad y cierre atómico de verificación.
- Compilación Android y suite unitaria: pendientes de GitHub Actions porque el entorno local no pudo descargar Gradle.
