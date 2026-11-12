# Coach 1.1.10.514 — Visor configurable, patrocinio por idioma y diagnóstico de consumo

Versión 1.1.10.514, build 1230.

## Cambios

- El interruptor de botones del visor del Hub se aplica también a la sesión del
  administrador. Las excepciones explícitas por cuenta siguen funcionando y el
  icono de diagnóstico queda dentro del mismo control de visibilidad.
- Los anuncios patrocinados permiten alcance general, un idioma o ambos idiomas.
  General y un solo idioma mantienen la publicación para toda la audiencia;
  Español + Português segmenta por idioma y duplica el precio mostrado y
  validado por el servicio. Las publicaciones antiguas conservan el alcance
  general.
- La pantalla de Inicio filtra solo las campañas de dos idiomas según el idioma
  elegido, sin ocultar campañas generales ni campañas de un solo idioma.
- El gestor de patrocinio y el sincronizador usan el mismo buzón local, con
  compatibilidad de lectura para solicitudes antiguas, evitando que una decisión
  aprobada o eliminada reaparezca por una copia local desfasada.
- El resumen de datos muestra únicamente categorías operativas actuales,
  elimina del resumen perfiles, esencias, dispositivos, canjes e historiales de
  suscripciones, y deja la configuración activa como consulta de solo lectura.
  Se conserva la eliminación individual de registros operativos con confirmación.
- El diagnóstico explícito añade duración de sesión, batería inicial y actual,
  porcentaje consumido, carga acumulada en mAh, energía en Wh, corriente,
  voltaje, potencia instantánea, salud de batería, antigüedad y dimensiones del
  último frame, FPS y rangos del escáner, tiempos de procesamiento, fotogramas
  recibidos/procesados/omitidos y origen de calibración. No inicia un sondeo de
  batería en segundo plano: mide al comenzar la sesión del overlay y al enviar.
- Se conservan los perfiles de calibración medidos por dispositivo, incluido
  Motorola moto g56 5G en 2400x1080 con API 36, para reutilizarlos como respaldo
  cuando coincidan fabricante, modelo y resolución.

## Archivos y verificaciones

- Datos y patrocinio: `AppNoticeManager`, `SponsorReviewRepository`,
  `SponsorSubmissionStore`, `server/economy/policy.mjs` y
  `server/economy/service.mjs`.
- Visor y telemetría: `FloatingOverlayContent`, `DraftVisionScanner`,
  `DeviceDiagnosticButton` y `DeviceDiagnosticTelemetry`.
- Datos operativos: `DatabaseStatisticsRepository`, `SavedDataDeletion`,
  `AdminDatabaseConsumptionDialog` y `SavedDataRecordsDialog`.
- Interfaz de Inicio y publicación: `MainDraftingScreen` y
  `SponsorCpmPanelDialog`.
- Compilación `:app:compileDebugKotlin`: aprobada con el SDK local.
- `npm test --prefix server/economy`: 52 pruebas aprobadas.
- `:app:testDebugUnitTest` dirigido a `SponsorReviewPolicyTest` y
  `OverlayAccessSnapshotTest`: aprobado. La suite completa de UI parametrizada
  se detuvo tras quedar bloqueada más de diez minutos en Robolectric; no se
  presenta como validación completa.
- `assembleRelease` no se ejecutó porque este entorno no contiene el keystore
  de producción ni sus variables privadas; la firma y el APK final quedan para
  la validación protegida del flujo de entrega.
- No se han eliminado claves, credenciales ni datos financieros.
