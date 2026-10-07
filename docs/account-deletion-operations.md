# Coach: gestión de eliminación de cuenta

Versión 1.1.10.217. Contacto público: DevWildRiftCoach@gmail.com.

## Solicitud en la aplicación

Usuario → Eliminar mi cuenta → primera confirmación → contraseña → confirmación final. El servicio registra una fecha autoritativa y la app cierra la sesión. Un nuevo inicio de sesión dentro de los siguientes 60 días cancela el pedido. Un inicio posterior al plazo no permite recuperar una cuenta cuyo borrado definitivo haya comenzado. No se borra la cuenta de Riot.

La tarea programada se ejecuta cada hora, en el minuto 17, y también después de cambios en la rama de publicación. Comprueba permisos antes de aceptar solicitudes, bloquea sesiones antes de purgar, elimina los datos asociados antes de eliminar la identidad y guarda puntos de reintento. La disponibilidad deja de aceptarse cuando su última comprobación tiene más de 36 horas. El plazo mínimo comprobado es de 60 días transcurridos; no se promete un instante exacto de finalización.

## Solicitud externa sin reinstalación

1. La página pública ofrece consecuencias, dos pasos de confirmación y un correo preparado. El usuario debe enviarlo; la página no crea automáticamente la solicitud.
2. El responsable atiende el correo y verifica que el solicitante controla el correo de su cuenta. Puede enviar una confirmación al correo registrado y pedir respuesta con el consentimiento. No solicitar contraseñas ni códigos de sesión; no aceptar un cambio de correo como prueba de identidad.
3. Con identidad y consentimiento acreditados, una persona autorizada ejecuta el comando siguiente usando la conexión de servicio ya configurada, en una terminal privada. No guardar los datos de la persona en registros públicos.

```bash
node server/account-deletion/request-verified-email.mjs CORREO_DE_LA_CUENTA --owner-identity-and-consent-verified
```

4. Confirmar al titular la fecha registrada y la fecha de recuperación límite. El plazo comienza al registrar la solicitud verificada, no al abrir el sitio. El correo se atiende manualmente; esta intervención no envía mensajes en nombre del responsable.
5. Revisar periódicamente fallos de la tarea y la bandeja de privacidad. Si existe un obstáculo real, informar al titular y tramitar el borrado conforme a la legislación aplicable. No reiniciar el plazo silenciosamente.

## Archivos antiguos y conservación

Los nuevos videos subidos al almacenamiento controlado incluyen un identificador verificable de su titular. Se revisan también subidas sin un aviso activo. Un archivo con otro propietario se conserva. Si un archivo antiguo carece de propietario, el proceso queda pendiente de revisión: comprobar la titularidad antes de retirar el recurso. Para antiguos archivos en servicios externos, solicitar retirada al proveedor y verificarla antes de cerrar la revisión. Tras verificar la retirada, retirar las referencias afectadas y su registro privado de revisión para que el siguiente reintento compruebe el resultado. Nunca marcar como completado un borrado que no se ha verificado.

El registro de revisión privada contiene únicamente los datos necesarios para resolver la solicitud. Tras la purga, el registro mínimo de seguridad permanece 24 horas para bloquear sesiones antiguas; la limpieza se comprueba periódicamente. Las exportaciones ya compartidas y las copias locales no se eliminan a distancia.

El responsable debe evaluar la compatibilidad del plazo de recuperación de 60 días y la conservación con las jurisdicciones de sus usuarios. Google Play no certifica ese plazo por tener un aviso. Si la ley exige atender antes una solicitud de supresión, debe existir un procedimiento que cumpla ese requisito.
