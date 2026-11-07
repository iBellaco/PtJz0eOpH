# Coach 1.1.10.489 — Visor del Hub, calibración por dispositivo y resolución inmediata de anuncios

## Diagnóstico

- El Hub había dejado ocultos los accesos del visor para sesiones autorizadas y
  el estado de Auto-Scan apenas se distinguía del resto del pie del panel.
- La calibración no tenía un guardado explícito y la selección de objetivos
  mezclaba columnas, puestos y baneos en una sola lista.
- La decisión de un anuncio podía consultar una colección completa dentro de la
  transacción. Esa lectura podía depender de un índice y enviar una aprobación
  o un rechazo válido a la cola de recuperación, mostrando un estado pendiente.

## Cambios implementados

- Se restauraron en el Hub los botones del visor y de diagnóstico para las
  sesiones autorizadas, conservando el acceso remoto para las demás sesiones.
- Auto-Scan ahora muestra un indicador animado, color de estado, punto activo y
  el texto `ACTIVO` cuando está funcionando.
- La calibración organiza Aliados y Rivales por columna, slots individuales y
  baneos. Los controles mantienen movimientos arriba, abajo, izquierda y
  derecha, ajuste de tamaño y pasos configurables.
- Se añadió `Guardar coordenadas en este dispositivo`. La geometría se persiste
  en el perfil de captura activo y sobrevive al cierre del Hub o del proceso.
- El informe de coordenadas incluye usuario, modelo exacto, dimensiones de
  pantalla, captura observada y coordenadas formateadas. Se registra como
  mensaje privado para revisión administrativa y no se copia a la bandeja del
  usuario.
- Las aprobaciones y rechazos de anuncios reconcilian las solicitudes mediante
  identificadores deterministas, sin consultas de colección dentro de la
  transacción. Se conservan la idempotencia, el reembolso único y la
  autorización firmada.
- Las reglas de acceso permiten crear informes de calibración privados y solo
  permiten su lectura al personal autorizado correspondiente.

## Archivos principales

- `app/src/main/java/com/example/service/FloatingOverlayContent.kt`
- `app/src/main/java/com/example/ui/components/CalibrationControls.kt`
- `app/src/main/java/com/example/data/CalibrationReport.kt`
- `app/src/main/java/com/example/ui/components/LiteRTEngineViewerDialog.kt`
- `app/src/main/java/com/example/ui/components/DraftCalibrationPanel.kt`
- `app/src/main/java/com/example/service/screen/DraftVisionScanner.kt`
- `app/src/main/java/com/example/data/FeedbackRepository.kt`
- `app/src/main/java/com/example/data/SupportConversationPolicy.kt`
- `app/src/main/assets/translations_pt.json`
- `server/economy/service.mjs`
- `firestore.rules`

## Verificaciones

- `npm test --prefix server/economy`: 52 pruebas unitarias superadas.
- Integración del servicio económico con emuladores locales: 30 casos
  superados, incluyendo publicación con cuenta patrocinadora y aprobación,
  rechazo, reembolso e idempotencia con cuenta administrativa.
- Pruebas de reglas aisladas con emuladores: 65 escenarios superados,
  incluyendo el informe de calibración privado.
- Prueba aislada de revisión de anuncios: superada.
- `node --check` superado para `service.mjs`, `queue.mjs` e `index.mjs`.
- `git diff --check` superado.
- La suite Android no pudo ejecutarse en este entorno porque no hay SDK de
  Android disponible; la compilación release y las pruebas Compose quedan para
  la validación de Actions.

## Alcance de operación

No se añadió monitoreo continuo ni sondeo permanente. Las decisiones se
confirman en una única llamada directa y la cola queda únicamente como
recuperación ante una indisponibilidad temporal del servicio.
