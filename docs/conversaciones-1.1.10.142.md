# Coach 1.1.10.142 (858)

## Activación manual

1. Instalar esta versión, también en las cuentas del equipo de soporte.
2. Reemplazar las reglas completas por el contenido de `firestore.rules` de esta versión y publicarlas. No mezclar con el bloque anterior: la consulta de moderadores exige una regla basada en `staffVisible`, sin una condición adicional sobre la etiqueta que la consulta no puede demostrar.
3. Abrir sugerencias y reportes una vez con una cuenta administradora. La migración identifica al propietario de tickets antiguos, conserva su historial y prepara su visibilidad y permiso de respuesta. No borra reportes.
4. Si hay errores de permisos, se muestra un aviso de carga fallida en lugar de aparentar que no existen mensajes.

## Comportamiento

- Una sola lista canónica por identificador; las solicitudes internas no se presentan como reportes. Cada actualización reemplaza la lista, retirando elementos eliminados. Dos reportes distintos con el mismo título permanecen distintos.
- El sistema saluda al crear el mensaje. El usuario espera una respuesta real del equipo antes de contestar. La validación se aplica a la interfaz, la transacción y las reglas compartidas.
- El equipo puede añadir respuestas sin editar el historial y cerrar la conversación. El cierre se comparte entre dispositivos y el usuario no puede reabrirla enviando una respuesta.
- Solo una cuenta administradora puede eliminar tickets y mensajes. La eliminación individual usa el identificador exacto, nunca el título.
- Se retiran los encabezados antiguos de identidad/contacto del cuerpo del mensaje. Los datos del remitente continúan disponibles para el equipo, separados del contenido.
- Las etiquetas de patrocinio siguen fuera del acceso de moderadores. No se modifica la función de streamers.

## Prueba manual

Enviar un reporte, confirmar un único saludo del sistema y que aún no se puede responder. Abrirlo con un moderador, responder dos veces y comprobar que ambas respuestas se conservan. Responder desde el usuario. Cerrar desde el equipo y confirmar el bloqueo en otro dispositivo. Comprobar que los moderadores no disponen de eliminación ni acceden a patrocinio. Crear dos reportes con el mismo título y eliminar uno: el otro debe conservarse. Comprobar que solicitudes internas no aparecen como sugerencias ni reportes. Repetir en portugués.
