# Firma persistente de Coach

El flujo anterior generaba una clave de depurado en cada ejecución porque no había una clave persistente guardada. Las versiones 160 y 161 tienen certificados diferentes; no pueden actualizarse una sobre otra directamente.

La corrección preparada exige antes de publicar desde `main`:

- Secreto de GitHub `COACH_DEBUG_KEYSTORE_BASE64`: archivo de firma original codificado en base64. No guardar claves privadas en el repositorio ni pegarlas en conversaciones.
- Variable de GitHub `COACH_SIGNING_CERT_SHA256`: SHA-256 del certificado público de esa clave. Sirve para impedir publicar con una identidad equivocada.
- Archivo compatible con el alias `androiddebugkey` y el formato de depurado usado por Coach. La contraseña estándar de depurado es `android`.

Las ejecuciones de prueba pueden usar claves temporales. Una publicación exige la clave persistente y su certificado correcto; nunca genera una clave sustituta silenciosamente.

La clave privada no se puede recuperar del certificado incluido en un APK. Si no existe una copia de la clave original, hace falta planificar una migración de firma y conservar los datos antes de cambiar de instalación. No borrar datos ni desinstalar la aplicación automáticamente.

Estado de esta sesión: no hay una clave disponible en el repositorio o el entorno. La conexión de GitHub devuelve HTTP 403 al acceder a secretos; la activación queda pendiente de una conexión autorizada o de configurar el secreto de forma segura.
