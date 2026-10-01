# Coach 1.1.10.141 — soporte y canales en vivo

El cambio de aplicación está en la solicitud de cambios de esta versión. Las nuevas reglas de acceso se validan automáticamente, pero ese flujo de pruebas no las publica en el servicio real.

## Activación

1. Instalar el APK de pruebas 1.1.10.141 con una cuenta administradora antes de la activación. Comprobar que el proyecto de destino es `wild-rift-drafting`.
2. Con una sesión del propietario del proyecto, revisar y guardar las reglas actualmente publicadas. Si contienen permisos adicionales que no están en este repositorio, conservarlos al integrar estos cambios. Después, publicar el contenido de `firestore.rules` en la pestaña de reglas de la consola del servicio, o ejecutar desde la raíz del repositorio:

   ```sh
   npx firebase-tools@13.35.1 login
   npx firebase-tools@13.35.1 deploy --only firestore:rules --project wild-rift-drafting
   ```

3. Abrir Soporte con esa cuenta en la aplicación actualizada. La migración asigna la visibilidad a los tickets existentes, recupera el identificador de su destinatario cuando el correo coincide con una cuenta e incorpora un historial inicial cuando aún no existe. No une tickets por título ni elimina mensajes por errores de carga. No cambia tickets que ya tienen historial.
4. Comprobar con una cuenta usuaria y una moderadora, en dos dispositivos, el envío, las respuestas sucesivas, leído y solucionado. Los tickets de patrocinadores deben verse únicamente por su remitente y la cuenta administradora.
5. Comprobar el panel Streamers dentro de las solicitudes de verificación y roles. Aprobar únicamente después de abrir el canal y comprobar manualmente que usa Coach durante la transmisión.
6. Tras validar la activación, integrar la solicitud de cambios para publicar la versión. Las cuentas que envíen mensajes o soliciten publicaciones deben usar la versión actualizada.

La sesión de GitHub no concede acceso al proyecto del servicio. No se debe presentar el cambio de reglas como publicado hasta comprobar su despliegue. Si no se puede relacionar un ticket antiguo con una cuenta mediante su correo, no se envía una respuesta a un destinatario adivinado.

Documentación de despliegue: https://firebase.google.com/docs/firestore/security/get-started#deploying_rules

## Comprobaciones

El flujo Android ejecuta las pruebas existentes y `SupportAndStreamerPolicyTest` y construye el APK. El flujo independiente de permisos ejecuta los escenarios de `tests/firestore/rules.test.mjs` en un proyecto de demostración, sin acceder a datos reales.

```sh
cd tests/firestore
npm ci
npm test
```

Casos cubiertos: acceso del remitente, separación de patrocinadores, consultas de moderación, bloqueo de autoasignación de roles, respuestas acumuladas, protección del saludo del sistema, lectura compartida, respuesta de seguimiento, cierre compartido, URL maliciosa, límite de cinco con aprobaciones concurrentes y retirada de la publicación propia.

## Reporte copiable para testers

Coach 1.1.10.141 (857)
Estado: preparado para pruebas de activación.
- Comprobar que los mensajes enviados aparecen en la bandeja y conservan su historial.
- Comprobar respuestas sucesivas, saludo del sistema y estados sincronizados entre dispositivos.
- Comprobar enlaces de canales, revisión de solicitudes, etiquetas en vivo y el límite de cinco publicaciones.
- Comprobar los textos de las funciones nuevas en español y portugués.

## Commit copiable

Corrige la bandeja y la sincronización de soporte y añade solicitudes de streamers en vivo
