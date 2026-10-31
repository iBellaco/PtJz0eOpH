# Firma persistente de Coach

Coach 164 inicia una identidad nueva, autorizada por el desarrollador después de
confirmar que no está disponible la clave anterior. La primera instalación no
puede actualizar un APK firmado con otra clave. No desinstalar ni borrar datos
automáticamente. Las versiones posteriores reutilizan la nueva identidad.

La clave nueva se conserva **únicamente en el cuerpo de un borrador privado de
GitHub**, identificado en `.github/coach-signing.json`. No está en el repositorio,
los artefactos de Actions, las cachés ni las publicaciones públicas. Solo los
colaboradores con acceso de escritura pueden acceder al borrador. Su página
respondió HTTP 404 sin autenticación al preparar esta migración.

**Nunca publicar ni eliminar ese borrador.** Publicarlo expondría la clave privada;
eliminarlo impediría firmar actualizaciones si no existe otra copia. Los flujos de
publicación de APK usan sus propias etiquetas de versión y nunca publican ese
identificador. La restauración rechaza cualquier documento que no conserve su
estado de borrador, su identificador y el certificado previsto. Guardar una copia
privada adicional con el propietario del proyecto cuando exista un canal seguro.

La huella SHA-256 pública de la identidad nueva es:
`27dba5165e26d0da19274edfd21a8f0439e6d7d7cc979efd449c7c71c7b4c055`.
El APK compilado se verifica con `apksigner` contra esa misma huella antes de
publicarse. La identidad no se regenera en cada ejecución.

Cuando se disponga de acceso a secretos, puede migrarse **la misma clave**, sin
cambiar de certificado, al secreto `COACH_DEBUG_KEYSTORE_BASE64` y la variable
`COACH_SIGNING_CERT_SHA256`. Los secretos configurados tienen prioridad. Esta
alternativa evita bloquear la publicación por los permisos actuales de la
conexión, que devuelve HTTP 403 al intentar acceder a secretos y variables.

El archivo usa el alias `androiddebugkey` y el formato de depurado de Coach. Los
archivos restaurados tienen permisos 0600. Los errores nunca imprimen el cuerpo
privado, el archivo de firma o la salida privada de `keytool`. Las pruebas de
propuestas de otros repositorios usan firma efímera y no solicitan la clave.

Las versiones antiguas 160 y 161 ya tenían certificados diferentes. Una clave
privada no puede recuperarse del certificado incluido en el APK. La autorización
para usar otra firma resuelve la publicación nueva, no la actualización de esas
instalaciones ni la activación de cambios de permisos en la nube.
