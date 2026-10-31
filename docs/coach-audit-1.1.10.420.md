# Coach 1.1.10.420 — build 1136

## Alcance y entrega

El propietario autorizó resolver todas las recomendaciones de la revisión de
la aplicación. Primero se verificó el éxito de `fix-error` 40958001122 para
8f73718b y se promovió exactamente ese commit a `coach-validacion`, PR #174.
Las modificaciones de este documento empiezan por separado en `fix-error`.
No se fusiona `main` ni se monitorean continuamente las ejecuciones.

## Correcciones

- Operaciones: espera breve por la confirmación de una operación encolada,
  distinción entre error, envío sin confirmar, espera y revisión. El diálogo
  de esencias puede cerrarse y no anuncia éxito sin resultado confirmado.
- Identificadores persistidos antes de enviar, con separación por cuenta,
  acción y contenido; los anuncios además conservan su identidad individual.
  Los reintentos consultan el resultado y reutilizan la misma operación.
  La bandeja local de anuncios conserva contenido, costo e identificador
  aunque se cierre la pantalla o termine el proceso.
- Causa comprobada del rechazo inicial: una transacción leía un documento
  inexistente, pero las reglas exigían datos de propietario en ese documento.
  Se permite consultar únicamente documentos ausentes del espacio propio;
  las consultas y los documentos existentes siguen restringidos al propietario.
  También se impide reemplazar una solicitud completada de otra cuenta.
- Una consulta por propietario muestra todas sus solicitudes, incluidos varios
  anuncios independientes, y los contadores cuentan cada solicitud pendiente.
  El panel distingue carga, copia sin confirmación, error, envío sin confirmar,
  pendiente de aprobación, activo, pausado, rechazado y reembolsado.
- Un rechazo nuevo conserva su registro y retira la publicación atómicamente.
  Se conserva la política anterior de no reembolsar un rechazo explícito:
  la tarea de devolución por falta de respuesta omite esos registros. Los
  rechazos borrados por versiones anteriores no se reconstruyen ni se inventan.
- Permisos visuales centralizados y reactivos: un texto de rol o un fragmento
  del correo ya no habilita administración. Se respetan las suspensiones, se
  ofrece revalidar la sesión y los callbacks de sesiones antiguas no cambian
  los permisos de la cuenta actual. No se crean claims desde el cliente.
- El Hub diferencia décima selección provisional, selección en curso y cierre
  confirmado. Señala roles rivales estimados y conserva las correcciones manuales.
  No se alteran umbrales, coordenadas ni el contrato de reconocimiento.
- Coaching: las recomendaciones utilizan los dos kits cuando se conoce un
  posible rival, las condiciones de la línea y el formato Soberano existente.
  Declaran que el draft no observa vida, oro, oleadas ni enfriamientos.
- Builds: prioridad a datos del campeón y su línea; sin relleno genérico por
  rol, sin dos objetos de misión de soporte y sin mezclar claves de runas.
  Una página de runas incompleta se identifica, no se inventa. Los objetos
  situacionales se priorizan dentro de las opciones propias del campeón.
- Nuevos textos en español, portugués y la variante brasileña de recursos.

## Archivos afectados

- `app/build.gradle.kts`.
- Datos: `EconomyServiceClient`, `EconomyRequestPolicy`, `PendingOperationIds`,
  `AccountOperationIds`, `AdminPremiumRepository`,
  `SponsorSubmissionStore`, `SponsorReviewRepository`, `DraftBuildPolicy`,
  `WildRiftRepository`.
- Sesión: `AuthManager`, `RolePanelAccess`, nuevo `SessionPanelAccess`; consumidores en
  `AdminDashboardDialog`, `AdminFeedbackPanel`, `AdminSupportReportsDialog`,
  `UserDetailManagementDialog`, `AvatarSelectionDialog`, `CustomBuildDetailDialog`,
  `ChampionBuildCreatorDialog`, `AuthScreen` y `FloatingOverlayContent`.
- Interfaz: `AdminGiveEssenceDialog`, `EconomyPendingStatus`, `SponsorCpmPanelDialog`.
- Draft: `OverlayState`, nuevo `DraftCertainty`, `CoachingGenerator`.
- Recursos: `values`, `values-pt`, `values-pt-rBR` / `strings.xml`.
- Seguridad y procesamiento: `firestore.rules`, `server/economy/maintenance.mjs`.
- Pruebas: `AppReliabilityTest`, `SponsorSubmissionStoreTest`,
  `EconomyRequestPolicyTest`, `InboxSynchronizationTest`, `SponsorReviewPolicyTest`,
  `tests/firestore/economy-submission.test.mjs`, `sponsor-review.test.mjs`,
  `run-tests.mjs`, `server/economy/test/emulator.integration.mjs`.
- `.github/workflows/fix-error.yml`: ejecuta las regresiones específicas nuevas,
  las pruebas económicas y los envíos con las reglas reales en emuladores aislados.

## Evidencia y límites

- Cuatro grupos de comprobaciones de envíos reales contra reglas emuladas:
  primer ajuste, varios anuncios, reintentos, privacidad y no escalamiento.
- Prueba de moderación: propietario, autorización, publicación atómica,
  rechazo persistente, no reaprobación y protección de devoluciones.
- 28 casos de integración del servicio económico, incluido rechazo sin devolución.
- 46 pruebas unitarias del servicio económico.
- 64 escenarios de reglas; sin acceso a datos de producción.
- 16 comprobaciones locales de las políticas Kotlin productivas de identidad,
  persistencia mediante interfaz de almacenamiento, permisos y certeza del draft.
- Análisis sintáctico de los Kotlin modificados. No equivale a compilar Android.
- Recursos completos, auditoría portuguesa sin residuos españoles, contenido del
  catálogo validado, 10 pruebas del plan de validación y `git diff --check`.
- Las nuevas pruebas JUnit/Robolectric quedan incluidas en `fix-error`.
  El intento local de `testDebugUnitTest` no pudo descargar Gradle: conexión
  rechazada. No hay SDK Android local y no se afirma haber compilado, instalado
  ni comprobado un APK de esta versión. La entrega ofuscada requiere validación.

El comportamiento de producción requiere publicar tanto aplicación como reglas
y procesamiento. Un verde de pruebas no demuestra que un servicio se desplegó
ni que el teléfono ya ejecuta la versión corregida. No se han entregado esencias,
publicado anuncios reales, migrado permisos ni ejecutado cambios en producción.

## Resumen copiable para testers

Coach 1.1.10.420: comprobar esencias con confirmación real, cierre y recuperación
de envíos sin duplicados, varios anuncios independientes y sus estados. Revisar
acceso por sesión, indicaciones provisionales del draft, líneas rivales estimadas,
consejos condicionados y builds sin relleno genérico, en español y portugués.
Pendiente de compilar y validar en dispositivo.

Commit: Corregir envíos, permisos, recuperación y recomendaciones de Coach
