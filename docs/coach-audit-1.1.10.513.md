# Coach 1.1.10.513 — Avisos por idioma, diagnóstico y gestión individual

Versión 1.1.10.513, build 1229.

## Cambios

- La ficha del comunicado se obtiene del idioma elegido en el diálogo. Si no
  existe uno activo para ese idioma, no se presenta una ficha de otro idioma.
- El título es opcional con multimedia horizontal en publicación y edición.
  Se alinearon validación del servicio, previsualización de Inicio y confirmación.
- Se muestran centavos en las proyecciones CPM. Se elimina el recargo involuntario
  sin anuncios activos y se limita a 50% el descuento por compartir rotación.
  Las proyecciones de tráfico no son una tarifa garantizada de mercado.
- La lista del visor funciona como excepciones a ambos interruptores globales.
  La activación espera las dos lecturas para evitar detener una excepción válida.
- El resumen permite consultar y eliminar registros individuales con confirmación.
  Se excluyen perfiles, suscripciones activas, saldos, dispositivos e historiales
  financieros/canjes para conservar la trazabilidad de las esencias. No hay borrado masivo.
- Icono de diagnóstico en la cabecera del Hub: envío explícito del nombre público,
  dispositivo, Android, dimensiones, FPS del escáner, última duración, fotogramas
  omitidos, batería, temperatura, ahorro de energía y memoria. No obtiene FPS del
  juego ni latencia de red. Etiqueta Diagnóstico, lectura solo administrativa.

## Archivos y comprobaciones

- Vistas: BroadcastAnnouncementDialog, AdminCpmAnalyticsDialog,
  SponsorCpmPanelDialog, AdminNoticeConfigDialog, MainDraftingScreen,
  AdminOverlayAccessDialog, AdminDatabaseConsumptionDialog, SavedDataRecordsDialog,
  FloatingOverlayContent, DeviceDiagnosticButton, AdminFeedbackPanel,
  AdminSupportReportsDialog y FeedbackReportCard.
- Datos: OverlayAccessPolicy, SavedDataDeletion, SponsorReviewRepository,
  SupportConversationPolicy; reglas de acceso y política de precios del servicio.
- Recursos españoles/portugueses y pruebas de idioma, visor, patrocinio y privacidad.
- Pruebas dirigidas locales de idioma, visor, patrocinio y conversaciones
  aprobadas. 52 pruebas de política del servicio aprobadas.
- Reglas, envío de solicitudes y moderación aprobados en emuladores locales.
  Los 31 casos de integración de economía también terminaron correctamente,
  tras actualizar la expectativa del nuevo precio.
- Recursos localizados y auditoría portuguesa aprobados. Pendiente validación
  completa del APK firmado e instalación física; no se presenta como ya realizada.
- Requiere desplegar las reglas y el servicio actualizados junto con la app para
  habilitar el nuevo tipo de reporte y aceptar las nuevas cotizaciones.
