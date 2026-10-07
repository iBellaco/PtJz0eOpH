# Auditoría de Coach — 1.1.10.217 (build 933)

## Resultado y alcance

Actualización de privacidad, términos, contacto confirmado DevWildRiftCoach@gmail.com y solicitud de eliminación con dos confirmaciones, verificación de contraseña y recuperación de 60 días. No se certifica autorización de Riot ni aprobación de Google Play. Las mejoras de reconocimiento, etiquetas del catálogo y nombres de emparejamientos de 1.1.10.216 se conservan.

## Correcciones

- Retiradas afirmaciones de legalidad absoluta, inexistencia de sanciones, seguridad absoluta, actualización permanente y licencia única de todas las bibliotecas.
- Declarados datos de cuenta, dispositivo, actividad, mensajes, fotografías, canjes, proveedores, diagnóstico local y métricas de contenido patrocinado.
- Aviso de captura antes del permiso de Android. Retirados permisos amplios de lectura de fotografías cuando ya se utiliza el selector.
- Eliminación: fecha del servicio, dos confirmaciones, contraseña, cierre de sesión, cancelación por nuevo inicio de sesión durante 60 días y procesamiento programado independiente de la instalación.
- Purga antes de borrar la identidad; bloqueo de sesiones antiguas; reintentos con puntos de progreso; conservación mínima de seguridad de 24 horas y limpieza periódica. No se borran datos de otra cuenta ni copias ya exportadas.
- Retiradas subidas alternativas de nuevos videos a servicios externos sin mecanismo verificable de retirada. Las nuevas subidas controladas incorporan identificador y ruta de propietario inmutables, protegidos contra suplantación. Los archivos antiguos ambiguos requieren revisión y el proceso no declara finalización falsa.
- Página legal pública en español y portugués y solicitud externa por correo sin reinstalar. Atención externa manual, con verificación de titular y consentimiento; el plazo inicia cuando se registra el pedido verificado.
- README reescrito con repositorio, versiones, fuentes y límites reales. Limpieza de referencias antiguas en configuración, reglas de optimización e inventarios de traducción; conservadas las claves de firma.

## Verificaciones comprobadas localmente

- 4 escenarios de permisos de archivos aprobados: coincidencia de titular, bloqueo de suplantación, propietario inmutable y restricción de invitados/rutas.
- 56 escenarios de reglas de acceso aprobados, incluidos aislamiento, fecha del servicio, plazo no alterable, reautenticación reciente, cancelación dentro del plazo y bloqueo tras comenzar el borrado.
- Integración de demostración: borrado de identidad y datos asociados, conservación de cuenta ajena, reintento idempotente y cancelación por inicio real de sesión y bloqueo de cierre falso ante un antiguo archivo externo, eliminación de solicitudes personales secundarias y limpieza de respuestas propias sin eliminar el texto del destinatario.
- 17 pruebas del servicio: límite exacto de 60 días, carreras de inicio de sesión, recuperación, registro mínimo y detección de antiguos archivos externos y limpieza de conversaciones compartidas. La lista y los resultados definitivos del APK se adjuntarán a la entrega cuando terminen las comprobaciones; aún no se acreditan aquí.

## Riesgos y requisitos pendientes

1. **Riot:** no existe autorización aportada para Coach. Los avisos no conceden licencia. Debe consultarse por escrito el uso de recursos, asistente, distribución y financiación para Wild Rift; no usar una clave de otro juego como prueba de permiso.
2. **Google Play:** no se ha revisado una cuenta ni enviado la app a la tienda. Faltan declaraciones reales, evaluación de permisos, pagos, acceso de revisión y resolución de publicación. La configuración histórica de firma directa y un APK no acreditan un AAB aceptado por la tienda.
3. **Plazo legal:** Google no fija ni aprueba específicamente los 60 días. Debe evaluarse la rapidez del proceso y la normativa de las jurisdicciones reales; no se conocen país ni razón social del responsable.
4. **Archivos previos:** existía código de subida anónima a proveedores ajenos. No se comprobó aquí qué archivos se publicaron o retiraron. No se afirma su eliminación; una solicitud afectada requiere revisión y retirada verificadas.
5. **Operación:** atender el correo, verificar titulares y vigilar fallos de la tarea es responsabilidad del operador. El proceso automático no responde el correo. El programador puede retrasar ejecuciones y desactivar la tarea tras inactividad prolongada del repositorio; debe mantenerse activo y supervisarse. Una interrupción retrasa la ejecución y obliga a resolverla; la app deja de aceptar pedidos cuando la comprobación del servicio está desactualizada.
6. **Licencias:** están pendientes el inventario de derechos de recursos, fuentes de estadísticas y condiciones de cada componente. Publicar el código no concede derechos de terceros.

Procedimiento: [eliminación de cuentas](account-deletion-operations.md). Pasos y fuentes oficiales: [Riot y Google Play](riot-google-play-authorization.md).

## Archivos modificados

- `.github/workflows/account-deletion.yml`
- `.github/workflows/build-apk.yml`
- `.github/workflows/firestore-rules.yml`
- `AGENTS.md`
- `INSTRUCCIONES_AGENTES.md`
- `README.md`
- `all_missing_keys.json`
- `app/applet/secrets.defaults.properties`
- `app/applet/secrets.properties`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/assets/translations_pt.json`
- `app/src/main/java/com/example/data/AccountDeletionPolicy.kt`
- `app/src/main/java/com/example/data/AccountDeletionRepository.kt`
- `app/src/main/java/com/example/ui/auth/AuthScreen.kt`
- `app/src/main/java/com/example/ui/auth/AuthViewModel.kt`
- `app/src/main/java/com/example/ui/components/AccountDeletionCard.kt`
- `app/src/main/java/com/example/ui/components/DonationDialog.kt`
- `app/src/main/java/com/example/ui/components/PrivacyPolicyDialog.kt`
- `app/src/main/java/com/example/ui/screens/FAQScreen.kt`
- `app/src/main/java/com/example/ui/screens/MainDraftingScreen.kt`
- `app/src/main/java/com/example/util/NoticeMediaStorageManager.kt`
- `app/src/main/java/com/example/util/SystemPermissionHelper.kt`
- `app/src/main/res/values-pt/account_deletion.xml`
- `app/src/main/res/values-pt/startup_information.xml`
- `app/src/main/res/values/account_deletion.xml`
- `app/src/main/res/values/startup_information.xml`
- `app/src/test/java/com/example/AccountDeletionPolicyTest.kt`
- `app/src/test/java/com/example/AccountDeletionRenderedTest.kt`
- `app/src/test/java/com/example/LocalizationSurfaceTest.kt`
- `clean_missing_pt.json`
- `docs/account-deletion-operations.md`
- `docs/coach-audit-1.1.10.217.md`
- `docs/riot-google-play-authorization.md`
- `filtered_ui_missing.json`
- `firebase.json`
- `firestore.rules`
- `legal-site/confirmar-eliminacion.html`
- `legal-site/eliminar.html`
- `legal-site/index.html`
- `legal-site/pt/confirmar-eliminacion.html`
- `legal-site/pt/eliminar.html`
- `legal-site/pt/index.html`
- `legal-site/style.css`
- `metadata.json`
- `missing_strings_full.json`
- `pt_translations_bulk.json`
- `secrets.defaults.properties`
- `secrets.properties`
- `server/account-deletion/ensure-legal-site.mjs`
- `server/account-deletion/media.mjs`
- `server/account-deletion/package-lock.json`
- `server/account-deletion/package.json`
- `server/account-deletion/policy.mjs`
- `server/account-deletion/preflight.mjs`
- `server/account-deletion/request-verified-email.mjs`
- `server/account-deletion/store.mjs`
- `server/account-deletion/test/emulator.integration.mjs`
- `server/account-deletion/test/media.test.mjs`
- `server/account-deletion/test/policy.test.mjs`
- `server/account-deletion/worker.mjs`
- `storage.rules`
- `tests/firestore/package.json`
- `tests/firestore/rules.test.mjs`
- `tests/firestore/run-tests.mjs`
- `tests/firestore/storage.test.mjs`
- `untranslated_strings.txt`

- `server/account-deletion/conversations.mjs`
- `server/account-deletion/test/conversations.test.mjs`

Los cinco casos de interacción de eliminación se ejecutan en Android sobre el APK release instalado: mantienen las comprobaciones de ambas confirmaciones, contraseña obligatoria, cancelación sin envío, un solo envío durante la espera y errores sin confirmación falsa. Se usan respuestas controladas, sin borrar cuentas reales. El simulador de ventanas no logró estabilizar el campo de contraseña; las dos comprobaciones renderizadas de información legal siguen en su conjunto original. La validación instalada es obligatoria antes de fusionar.

- `app/src/androidTest/java/com/example/AccountDeletionInstalledTest.kt`
- `tools/verify-account-deletion-device.py`
