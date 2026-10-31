# Coach 1.1.10.232 — código 948

## Cambios

- `firestore.rules` y sus pruebas: los usuarios solo pueden marcar como leídos los avisos de su bandeja. La copia de una conversación solo se actualiza junto con el registro principal de soporte y conserva identidad, asunto, contenido y etiqueta.
- `UserInboxDialog.kt` y `translations_ui_pt.json`: una solicitud en estado `REVIEW` ahora indica que requiere revisión y pide contactar a Soporte; los demás estados pendientes mantienen su mensaje automático.
- `server/economy/queue.mjs` y su prueba: las consultas de respaldo respetan el límite solicitado antes de procesar registros, evitando cargar una cola completa cuando falta un índice.
- `build-apk.yml`: se comprueban los catálogos generados y el contenido en portugués antes de las validaciones Android de mayor duración.
- `app/build.gradle.kts`: versión 1.1.10.232, código 948.

## Verificación

- `npm test` en `server/economy`: 44 pruebas correctas.
- `npm test` en `server/account-deletion`: 18 pruebas correctas.
- `npm test` en `tests/firestore`: 56 escenarios de reglas, recuperación de cuenta y economía correctos en emuladores.
- `python3 tools/update-champion-builds.py --check`, `bash tools/audit-portuguese.sh`, revisión YAML y `git diff --check`.
- La compilación, firma, ofuscación e instalación del APK se ejecutarán en las comprobaciones obligatorias de GitHub antes de publicar la entrega.

## Resumen para testers

Coach 1.1.10.232: comprobar que un aviso de solicitud en revisión diga que requiere revisión y que se contacte con Soporte. Verificar que marcar un aviso como leído funciona en dos dispositivos y que no se puede cambiar su asunto ni contenido desde la aplicación.
