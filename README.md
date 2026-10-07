# Coach

Aplicación Android independiente de consulta y asistencia para la selección de campeones de League of Legends: Wild Rift. Nombre de la aplicación: **Coach**. Idiomas seleccionables: español y portugués. Versión: **1.1.10.217**, código **933**.

## Funciones implementadas

- Reconocimiento de nombres mediante OCR y comparación de retratos locales sobre la pantalla autorizada por Android. El décimo pick también usa el retrato; la precisión depende de la imagen, la posición y los recursos disponibles. Las detecciones inciertas pueden dejar la selección pendiente.
- Asistente flotante con draft, tier list e historial. La captura y la superposición requieren permisos de Android; se puede detener la captura desde el asistente o el sistema.
- Catálogo comunitario de campeones, objetos, runas, hechizos y mapas. Recomendaciones por campeón y línea, objetos esenciales y situacionales. Las relaciones de ventaja, debilidad y sinergia muestran el nombre al tocarlas.
- Consulta de tres fuentes identificadas: BestBuildWR, WildRiftFire y WildRiftCore. Son fuentes globales; el nombre histórico del componente de sincronización no acredita estadísticas oficiales del servidor chino. Ante fallos se puede conservar la última consulta guardada.
- Perfiles, historial, mensajes de soporte, avisos, contenido patrocinado, canjes y exportaciones locales según las condiciones disponibles para la cuenta.

No se garantiza exactitud completa del catálogo, reconocimiento perfecto, actualización inmediata por parche, ausencia de sanciones ni compatibilidad con todos los dispositivos.

## Eliminación de cuenta

En **Usuario → Eliminar mi cuenta**, la persona confirma dos veces y verifica su contraseña. La solicitud usa la fecha del servicio y cierra la sesión. Durante **60 días**, un nuevo inicio de sesión explícito cancela la eliminación. Restaurar la aplicación o renovar una sesión no equivale a esa confirmación.

Al cumplirse el plazo, un servicio programado procesa el borrado de la identidad y sus datos asociados, sin depender de que la aplicación siga instalada. Los fallos se reintentan; no se informa de borrado completo antes de finalizar. Los archivos antiguos sin propietario verificable o publicados en servicios externos requieren revisión y retirada. Las exportaciones y copias que el usuario haya compartido no se eliminan a distancia. Un registro mínimo de seguridad se conserva 24 horas después de completar el borrado y luego se limpia periódicamente.

También existe una solicitud externa por correo, sin reinstalar la aplicación: [eliminar cuenta](https://coach-legal-wild-rift-drafting.web.app/eliminar). Su atención es manual: el responsable verifica al titular, registra la solicitud y confirma la fecha. Abrir el correo no registra automáticamente un pedido. Procedimiento operativo: [gestión de solicitudes](docs/account-deletion-operations.md).

## Privacidad y situación legal

[Privacidad y términos en español](https://coach-legal-wild-rift-drafting.web.app/) · [Português](https://coach-legal-wild-rift-drafting.web.app/pt/)

Contacto público: **DevWildRiftCoach@gmail.com**. Nunca enviar contraseñas, códigos de acceso ni credenciales de Riot.

Coach no está respaldado ni patrocinado por Riot Games. Los recursos de Wild Rift conservan los derechos de sus titulares. **No se ha acreditado una autorización específica de Riot para esta aplicación.** Una clave de otro juego, el acceso al código o este aviso no conceden esa autorización. Antes de presentar la app en una tienda o monetizarla, deben resolverse las autorizaciones aplicables y completarse las declaraciones exigidas por la tienda. El plazo de 60 días es una decisión del producto, no un plazo aprobado expresamente por Google ni una garantía de cumplimiento de las leyes locales.

Consulta la [guía de autorización de Riot y publicación en Google Play](docs/riot-google-play-authorization.md) y la [auditoría de esta versión](docs/coach-audit-1.1.10.217.md). El repositorio no permite certificar aprobación legal ni de Google Play. No se afirma que todos sus componentes compartan una misma licencia.

## Desarrollo y comprobaciones

Repositorio: <https://github.com/iBellaco/PtJz0eOpH>. Ramas de trabajo: `pruebas` y `main`.

Configuración: Kotlin **2.2.10**, Gradle **9.3.1**, Java **21**, Android mínimo **API 24**, compilación y destino **API 36**, Jetpack Compose y Material 3. Las versiones exactas se mantienen en `gradle/libs.versions.toml` y los archivos Gradle.

```bash
git clone https://github.com/iBellaco/PtJz0eOpH.git
cd PtJz0eOpH
```

El wrapper no está versionado. Para una compilación local se necesitan Gradle, Java, Android SDK y la configuración privada de los servicios y firma. No publicar credenciales ni sustituir la identidad de firma de entregas anteriores. Los flujos de GitHub Actions preparan esas herramientas, comprueban el código, generan el APK release con R8 y verifican firma, ofuscación e instalación antes de publicarlo.

Pruebas del servicio y reglas en un proyecto de demostración aislado:

```bash
cd server/account-deletion
npm ci
npm test
cd ../../tests/firestore
npm ci
npm test
```

El servicio programado solo se activa con una conexión privilegiada verificada. Si deja de comprobarse su disponibilidad, la app no acepta nuevas solicitudes. Los resultados efectivos de validación se registran en la auditoría; la existencia del flujo no demuestra por sí sola que haya pasado.
