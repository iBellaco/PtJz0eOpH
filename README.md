# Coach

Aplicación Android independiente de consulta y asistencia para la selección de campeones de League of Legends: Wild Rift. Nombre de la aplicación: **Coach**. Idiomas seleccionables: español y portugués. Versión: **1.1.10.230**, código **946**.

El identificador instalado y usado para las actualizaciones es **com.Coach**. **com.example** es el namespace del código Kotlin, no otro paquete instalable. La firma persistente identifica al certificado como **CN=Coach, O=Coach, C=US**; no acredita una autorización de Riot ni de Google.

La configuración privada de Firebase incluye también un cliente Android llamado **com.aistudio.wildriftdrafting.wrdftx**, además de **com.Coach**. Ese registro explica el otro nombre encontrado al revisar los servicios, pero no cambia el identificador del APK distribuido. Los dos identificadores representan clientes distintos; no son alias intercambiables para instalar actualizaciones o registrar la aplicación en Google Play.

Este repositorio se utiliza para compilar el APK y respaldar el desarrollo. Su visibilidad pública no equivale a una licencia de software libre. El proyecto no ofrece una licencia de distribución o reutilización fuera de los permisos de la plataforma y los términos aplicables de terceros. Un nombre poco reconocible no impide consultar o copiar un repositorio público.

## Funciones implementadas

- Reconocimiento de nombres mediante OCR y comparación de retratos locales sobre la pantalla autorizada por Android. El décimo pick también usa el retrato; la precisión depende de la imagen, la posición y los recursos disponibles. Las detecciones inciertas pueden dejar la selección pendiente.
- Asistente flotante con draft, tier list e historial. La captura y la superposición requieren permisos de Android; se puede detener la captura desde el asistente o el sistema.
- Catálogo comunitario de campeones, objetos, runas, hechizos y mapas. Recomendaciones por campeón y línea, objetos esenciales y situacionales. Las relaciones de ventaja, debilidad y sinergia muestran el nombre al tocarlas.
- Consulta de clasificaciones globales de BestBuildWR (`/tierlist`), WildRiftFire y WildRiftCore. No existe integración activa con LOLM/Tencent ni estadísticas verificadas por elo obtenidas de esas consultas. La sincronización actualiza categorías sin convertirlas en porcentajes de victorias, selección o bloqueo. Conserva los números incluidos en el catálogo, cuya exactitud externa no se certifica aquí. Ante fallos se puede conservar la última consulta guardada.
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

Consulta la [guía de autorización de Riot y publicación en Google Play](docs/riot-google-play-authorization.md) y la [auditoría de esta versión](docs/coach-audit-1.1.10.221.md). El repositorio no permite certificar aprobación legal ni de Google Play. No se afirma que todos sus componentes compartan una misma licencia.

## Desarrollo y comprobaciones

Repositorio: <https://github.com/iBellaco/PtJz0eOpH>. Ramas de trabajo: `pruebas` y `main`.

Configuración: Kotlin **2.2.10**, Gradle **9.3.1**, Java **21**, Android mínimo **API 24**, compilación y destino **API 36**, Jetpack Compose y Material 3. Las versiones exactas se mantienen en `gradle/libs.versions.toml` y los archivos Gradle.

```bash
git clone https://github.com/iBellaco/PtJz0eOpH.git
cd PtJz0eOpH
```

El wrapper de Gradle 9.3.1 está versionado, con comprobación de la suma oficial del JAR y de la distribución. Para una compilación local se necesitan Java 21, Android SDK y la configuración privada de los servicios y firma. Después de configurarlos: `./gradlew :app:assembleDebug` para desarrollo y `./gradlew :app:assembleRelease` para una entrega ofuscada. No publicar credenciales ni sustituir la identidad de firma de entregas anteriores. Los flujos de GitHub Actions preparan esas herramientas, comprueban el código, generan el APK release con R8 y verifican firma, ofuscación e instalación antes de publicarlo.

La configuración Android `google-services.json` se obtiene del Secret de Actions `COACH_GOOGLE_SERVICES_JSON`; contiene el JSON completo de la aplicación `com.Coach`. El propietario confirma haber creado el Secret y Actions verificó su restauración en la entrega 219. El rechazo HTTP 403 previo correspondía al intento de esta conexión de crearlo, no a un fallo actual de compilación. El workflow no incluye sus valores ni los imprime. Sin el Secret, la compilación se detiene con un error explícito. Para desarrollo local, el archivo se coloca en `app/google-services.json`, excluido de Git. La restauración prioriza el Secret `COACH_DEBUG_KEYSTORE_BASE64` y conserva como alternativa el borrador privado descrito en [firma persistente](docs/persistent-apk-signing.md), manteniendo el certificado público de `.github/coach-signing.json`. No se restaura desde archivos base64 versionados. `secrets.properties`, claves JKS y keystore, base64, configuración cliente y credenciales temporales quedan excluidos. `secrets.defaults.properties` solo contiene comentarios públicos. La clave de configuración cliente sigue estando en el APK: Secrets evita publicarla en el workflow, pero no la convierte en una credencial de servidor ni sustituye las reglas de acceso. Los valores publicados anteriormente permanecen en commits y registros históricos; esta limpieza no reescribe el historial ni rota la clave.

Los scripts mantenidos están en `tools/` y `.github/scripts/`. Los scripts anteriores de traducción están en `tools/legacy/`; se retiró la copia obsoleta de `app/applet` y el marcador de conexión de la JVM. Los archivos ya ignorados no permanecen en el índice actual. Las pantallas y el asistente flotante están divididos por sección, preservando sus nombres de funciones para los consumidores Kotlin.

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

## Operaciones de esencias y entrega

Las compras Premium, las solicitudes de canje USDT, los ajustes de esencias, las concesiones de tiempo, las suscripciones a creadores y el envío de anuncios envían una solicitud privada al procesador `server/economy/worker.mjs`, ejecutado con la identidad de servidor ya configurada. El servidor comprueba la identidad, el rol permitido, la suspensión, la eliminación de cuenta, el saldo, los precios y las comisiones. Una transacción guarda el movimiento y su comprobante juntos. Los reintentos reutilizan su identificador; un rechazo de canje solo reembolsa una vez. Las solicitudes se atienden por antigüedad. Tras errores sin confirmación, el mismo identificador se reintenta de forma acotada y luego queda marcado para revisión manual, sin permitir un segundo cargo automático. Marcar un canje pagado sigue siendo una confirmación manual: este servicio no transmite criptomonedas ni custodia claves de billeteras.

La autorización de administración requiere el claim firmado `admin: true`; los correos, marcos y campos del perfil no lo conceden. La migración explícita del titular verificado y la activación se ejecutan con la conexión privilegiada ya configurada, mediante **Support and streamer permissions → Run workflow**, con `bootstrap_email` solo cuando el titular aún no tiene el claim. No publicar reglas restrictivas si falla la verificación del procesador o no existe al menos una identidad autorizada. No exige habilitar Cloud Build ni contratar el plan de pago: utiliza el servicio programado del repositorio público y las cuotas disponibles de la cuenta actual. El horario se solicita cada cinco minutos y puede demorarse. La app conserva una sola solicitud pendiente por cuenta y no confirma el movimiento antes de recibir el resultado. Cloud Functions queda como transporte opcional para una migración futura, sin activación automática. La guía [operación y activación](docs/economy-operations.md) distingue código comprobado y activación real.

Antes de compilar un APK, el flujo de entrega ejecuta las reglas y operaciones en emuladores aislados, las pruebas del servicio y `python3 test_matchup_rules.py` y `python3 test_scraper.py`. Los dos últimos ejecutan los tests reales del catálogo y del parser Kotlin con fixtures locales; no dependen de una página externa ni consideran válido un error de red. Después se ejecutan lint y toda la suite de pruebas unitarias de Android, seguidos por las comprobaciones de pantallas, firma R8 e instalación. Las ejecuciones de entrega y las del servicio conservan historiales separados.

Cada entrega final publica dos archivos ZIP descargables mediante HTTPS: **APK ofuscado** (APK, suma y pruebas de firma/ofuscación) y **proyecto** (código versionado y wrapper, sin APK ni archivos privados). Los enlaces locales de un entorno de trabajo no sustituyen esos archivos públicos. La verificación final debe descargar los ZIP y comprobar su integridad antes de compartirlos.
