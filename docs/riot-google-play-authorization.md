# Coach: autorización de Riot y publicación en Google Play

Revisión de fuentes oficiales: 2026-10-07. Contacto del proyecto: DevWildRiftCoach@gmail.com. Esta guía no acredita una autorización concedida ni sustituye una revisión jurídica del proyecto.

## Riot Games y Wild Rift

1. Entra a https://developer.riotgames.com/ con tu cuenta de Riot. Registra el producto con **Register Product / Register Project**, según la opción mostrada. Describe Coach como aplicación Android para Wild Rift; no lo presentes como un producto de League of Legends para obtener una clave que no corresponda.
2. Prepara una aplicación funcional, una página pública del producto, privacidad, términos y un video de sus funciones. Explica captura autorizada, OCR local, comparación de retratos, selección décima, ventana flotante, estadísticas y recomendaciones. El repositorio por sí solo no satisface la solicitud de clave de producción. La página legal publicada no sustituye una página que muestre el producto y una demostración funcional.
3. Expón todos los recursos de Riot utilizados (retratos, iconos, nombres y datos), fuentes de estadísticas, distribución directa y eventual Google Play, contenido patrocinado, apoyos voluntarios, canjes y acceso condicionado. No omitas funciones para conseguir aprobación.
4. En la documentación pública revisada no se encontró una API específica de Wild Rift. Abre una solicitud en **Developer Relations → Submit a request**: https://support-developer.riotgames.com/hc/en-us/requests/new. Pregunta qué autorización escrita y qué registro corresponden para Wild Rift, tanto para los recursos como para el asistente y su distribución/financiación.
5. Solicita una respuesta escrita con el alcance autorizado: juego, recursos, funciones, monetización, canales de distribución, condiciones y avisos obligatorios. Conserva el expediente. Hasta recibirla, no declares «aprobado por Riot» ni «sin riesgo de sanción».
6. Si Riot habilita una API pertinente y aprueba el producto, solicita la **Production API Key** del proyecto. La clave de desarrollo caduca a las 24 horas; las claves personales o de desarrollo no habilitan una app pública. Una clave de otro juego no prueba permiso para Wild Rift. Una clave tampoco equivale a patrocinio ni reemplaza las condiciones de propiedad intelectual.
7. Si se obtiene una clave, úsala desde un servicio seguro; nunca incluirla en el APK, repositorio, capturas o soporte. Si Riot rechaza una función o exige cambios, ajusta el producto al alcance autorizado antes de su distribución correspondiente.

Borrador de consulta para que el responsable lo envíe, completando los enlaces reales:

> Solicito orientación y autorización escrita para Coach, una aplicación Android independiente de asistencia en la selección de campeones de Wild Rift. Procesa la captura autorizada en el dispositivo mediante OCR y comparación de retratos, y muestra una ventana flotante con recomendaciones. Usa nombres, retratos e iconos asociados al juego. No he localizado una API pública específica de Wild Rift. Adjunto demostración funcional, página del producto, política de privacidad, términos e inventario de recursos. La app contempla contenido patrocinado, apoyos voluntarios y canjes; detallo sus condiciones y los canales de distribución previstos. ¿Qué registro, licencia y condiciones requieren estas funciones y recursos para Wild Rift y Google Play? ¿Qué funciones o formas de financiación deben modificarse? Solicito confirmar por escrito el alcance permitido. No afirmo que el proyecto esté aprobado actualmente.

Fuentes oficiales:

- [Política de propiedad intelectual y distribución de Riot](https://www.riotgames.com/en/legal): la publicación de apps con sus recursos en tiendas y la financiación tienen condiciones específicas. El aviso de independencia no concede una licencia.
- [Portal de desarrolladores](https://developer.riotgames.com/docs/portal): registro y clases de claves.
- [Solicitud de clave de producción](https://support-developer.riotgames.com/hc/en-us/articles/22801383038867-Production-Key-Applications): aplicación funcional, sitio y materiales del producto.
- [Política de aplicaciones de terceros, incluyendo Wild Rift](https://support.riotgames.com/en-us/riot/events/third-party-applications): la evaluación incluye ventajas y funciones, no solo si la app lee memoria. No permite certificar este asistente como inocuo automáticamente.
- [Condiciones de API](https://support-developer.riotgames.com/hc/en-us/articles/22698917218323-API-Terms-and-Conditions).

## Google Play

No existe una «clave legal» de Google Play que autorice recursos de Riot. Son trámites distintos: identidad y cuenta de desarrollador, firma de la app, declaraciones, pruebas y revisión de publicación.

1. Entra a https://play.google.com/console/signup con una cuenta de Google. Elige cuenta personal u organización según tu situación real, acepta el acuerdo, paga la cuota única de **25 USD** y completa la verificación de identidad. Las organizaciones requieren datos adicionales, incluido D-U-N-S cuando corresponda. Las cuentas personales nuevas también deben verificar acceso a un dispositivo Android.
2. Crea la app **Coach** y completa ficha, contacto, clasificación de contenido, público objetivo, publicidad, acceso para revisión y política de privacidad. Proporciona instrucciones o una cuenta de prueba que permita revisar las funciones restringidas.
3. En **Contenido de la aplicación → Seguridad de los datos**, declara los datos y finalidades reales: cuenta, identificadores de dispositivo, mensajes y fotos de soporte, actividad, canjes y métricas. No marcar «no recopilamos datos» por usar proveedores ni por procesar OCR localmente. Revisa los datos de cada SDK incluido.
4. Usa la política pública https://coach-legal-wild-rift-drafting.web.app/ y la URL externa https://coach-legal-wild-rift-drafting.web.app/eliminar en el apartado de eliminación de cuentas. Explica el plazo de 60 días, la cancelación al iniciar sesión, la atención manual externa y la conservación mínima. No afirmar que Google aprobó el plazo: exige un proceso razonablemente rápido, y la ley local puede exigir un plazo diferente.
5. Declara y demuestra el uso de captura de pantalla y servicio en primer plano cuando Play lo solicite. El aviso previo explica el acceso; el permiso de Android lo autoriza. Revisa contenido patrocinado, canjes, donaciones y cobros bajo las políticas de pagos. Los puntos ganados y comprados tienen reglas distintas; no implementar cobros externos de bienes digitales suponiendo una excepción general.
6. Prepara un **Android App Bundle (AAB)** release y configura **Play App Signing**. La clave de carga se crea y conserva de forma privada; no se solicita a Riot. La firma persistente usada para entregas directas debe revisarse antes de dar de alta el producto en Play: la configuración actual admite la identidad histórica con alias de depuración. No reemplazarla sin plan de continuidad, ya que Android rechaza actualizaciones con otra firma. El APK de entrega no acredita que ya exista un AAB o una configuración aceptados por Play.
7. Si tu cuenta personal se creó después del **13 de noviembre de 2023**, realiza una prueba cerrada con **12 testers** que permanezcan inscritos continuamente **14 días**, y después solicita acceso a producción contestando las preguntas. El cumplimiento de ese mínimo no garantiza aprobación.
8. Envía la versión y declaraciones a revisión. Solo una resolución efectiva permite decir que se aprobó esa publicación. La aprobación de Play no concede por sí sola una licencia sobre recursos de Riot.

Fuentes oficiales:

- [Alta, cuota e identidad](https://support.google.com/googleplay/android-developer/answer/6112435?hl=es).
- [Prueba cerrada para cuentas personales nuevas](https://support.google.com/googleplay/android-developer/answer/14151465?hl=es).
- [Eliminación de cuenta dentro y fuera de la app](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en).
- [Datos del usuario y política de privacidad](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en).
- [Permisos de fotos y videos](https://support.google.com/googleplay/android-developer/answer/14115180?hl=en-CA).
- [Pagos](https://support.google.com/googleplay/android-developer/answer/10281818?hl=en).
- [Firma de aplicaciones](https://developer.android.com/studio/publish/app-signing?hl=es-419).

## Pendientes reales

No se aportó licencia o aprobación de Riot, resolución de Google Play, país del responsable ni revisión de las jurisdicciones del servicio. El correo público está confirmado; no se inventó una razón social. También se deben comprobar los derechos de las fuentes de estadísticas y las licencias/avisos de las dependencias y recursos. La integración de borrado y una política visible resuelven funciones concretas; no permiten certificar cumplimiento jurídico total.
