# Auditoría de Coach — 1.1.10.217 (build 933)

## Resultado y alcance

Actualización de privacidad, términos, contacto confirmado DevWildRiftCoach@gmail.com y solicitud de eliminación con dos confirmaciones, verificación de contraseña y recuperación de 60 días. No se certifica autorización de Riot ni aprobación de Google Play. Las mejoras de reconocimiento, etiquetas del catálogo y nombres de emparejamientos de 1.1.10.216 se conservan.

## Correcciones

- Retiradas afirmaciones de legalidad absoluta, inexistencia de sanciones, seguridad absoluta, actualización permanente y licencia única de todas las bibliotecas.
- Declarados datos de cuenta, dispositivo, actividad, mensajes, fotografías, canjes, proveedores, diagnóstico local y métricas de contenido patrocinado.
- Aviso de captura antes del permiso de Android. Retirados permisos amplios de lectura de fotografías cuando ya se utiliza el selector.
- Eliminación: fecha del servicio, dos confirmaciones, contraseña, cierre de sesión, cancelación por nuevo inicio de sesión durante 60 días y procesamiento programado independiente de la instalación.
- Purga antes de borrar la identidad; bloqueo de sesiones antiguas; reintentos con puntos de progreso; conservación mínima de seguridad de 24 horas y limpieza periódica. No se borran datos de otra cuenta ni copias ya exportadas.
- Retiradas subidas alternativas de nuevos videos a servicios externos sin mecanismo verificable de retirada. Las nuevas subidas controladas incorporan identificador de propietario. Los archivos antiguos ambiguos requieren revisión y el proceso no declara finalización falsa.
- Página legal pública en español y portugués y solicitud externa por correo sin reinstalar. Atención externa manual, con verificación de titular y consentimiento; el plazo inicia cuando se registra el pedido verificado.
- README reescrito con repositorio, versiones, fuentes y límites reales. Limpieza de referencias antiguas en configuración, reglas de optimización e inventarios de traducción; conservadas las claves de firma.

## Verificaciones comprobadas localmente

- 56 escenarios de reglas de acceso aprobados, incluidos aislamiento, fecha del servicio, plazo no alterable, reautenticación reciente, cancelación dentro del plazo y bloqueo tras comenzar el borrado.
- Integración de demostración: borrado de identidad y datos asociados, conservación de cuenta ajena, reintento idempotente y cancelación por inicio real de sesión y bloqueo de cierre falso ante un antiguo archivo externo.
- 14 pruebas del servicio: límite exacto de 60 días, carreras de inicio de sesión, recuperación, registro mínimo y detección de antiguos archivos externos. La lista y los resultados definitivos del APK se adjuntarán a la entrega cuando terminen las comprobaciones; aún no se acreditan aquí.

## Riesgos y requisitos pendientes

1. **Riot:** no existe autorización aportada para Coach. Los avisos no conceden licencia. Debe consultarse por escrito el uso de recursos, asistente, distribución y financiación para Wild Rift; no usar una clave de otro juego como prueba de permiso.
2. **Google Play:** no se ha revisado una cuenta ni enviado la app a la tienda. Faltan declaraciones reales, evaluación de permisos, pagos, acceso de revisión y resolución de publicación. La configuración histórica de firma directa y un APK no acreditan un AAB aceptado por la tienda.
3. **Plazo legal:** Google no fija ni aprueba específicamente los 60 días. Debe evaluarse la rapidez del proceso y la normativa de las jurisdicciones reales; no se conocen país ni razón social del responsable.
4. **Archivos previos:** existía código de subida anónima a proveedores ajenos. No se comprobó aquí qué archivos se publicaron o retiraron. No se afirma su eliminación; una solicitud afectada requiere revisión y retirada verificadas.
5. **Operación:** atender el correo, verificar titulares y vigilar fallos de la tarea es responsabilidad del operador. El proceso automático no responde el correo. Una interrupción retrasa la ejecución y obliga a resolverla; la app deja de aceptar pedidos cuando la comprobación del servicio está desactualizada.
6. **Licencias:** están pendientes el inventario de derechos de recursos, fuentes de estadísticas y condiciones de cada componente. Publicar el código no concede derechos de terceros.

Procedimiento: [eliminación de cuentas](account-deletion-operations.md). Pasos y fuentes oficiales: [Riot y Google Play](riot-google-play-authorization.md).
