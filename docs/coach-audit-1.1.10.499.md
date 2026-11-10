# Auditoría de corrección de reportes, coordenadas y anuncios de Coach 1.1.10.499

## Versión

- `versionName`: `1.1.10.499`
- `versionCode`: `1215`
- Alcance: apertura inicial de la bandeja de soporte, presentación compacta de
  coordenadas, eliminación de reportes y publicación persistente de anuncios.

## Correcciones aplicadas

- La bandeja de soporte se abre como una ventana estable a pantalla completa,
  sin el cierre accidental de la primera interacción ni el panel inferior
  parcialmente visible.
- Los informes de coordenadas muestran únicamente el nombre de usuario, marca
  y modelo, dimensiones de pantalla, versión de Android y coordenadas. La
  etiqueta visible queda reducida a `COORDENADAS`; los marcadores técnicos se
  conservan solo para copiar y volver a interpretar el informe.
- El nombre se obtiene del perfil de Coach y no se sustituye por iniciales de
  correo. El formato portugués usa sus etiquetas correspondientes.
- Los botones de copiar, expandir y eliminar tienen áreas táctiles de 48 dp.
  La eliminación borra primero el reporte canónico y deja la limpieza de
  espejos antiguos como operación independiente para no bloquear la acción.
- La aprobación o el rechazo de un anuncio actualiza siempre el estado de la
  solicitud, el registro público y el documento de publicación. Las filas de
  cola antiguas ya no pueden impedir la decisión directa ni volver a mostrarla
  como pendiente.

## Archivos modificados

- `app/build.gradle.kts`
- `app/src/main/java/com/example/data/CalibrationReport.kt`
- `app/src/main/java/com/example/data/FeedbackRepository.kt`
- `app/src/main/java/com/example/data/SponsorReviewRepository.kt`
- `app/src/main/java/com/example/ui/components/AdminFeedbackPanel.kt`
- `app/src/main/java/com/example/ui/components/AdminSupportReportsDialog.kt`
- `app/src/main/java/com/example/ui/components/CalibrationControls.kt`
- `app/src/main/java/com/example/ui/components/FeedbackReportCard.kt`
- `app/src/main/java/com/example/ui/components/SupportReplyDialog.kt`
- `app/src/test/java/com/example/CalibrationReportTest.kt`

## Verificaciones

- `./gradlew :app:testDebugUnitTest --tests com.example.CalibrationReportTest --tests com.example.SponsorReviewPolicyTest --tests com.example.SponsorNoticeMergePolicyTest`
- Prueba de interfaz `RuntimeVisibilityTest` para la apertura de moderación.
- `node --test server/economy/test/policy.test.mjs` (48 pruebas correctas).
- `git diff --check`.
