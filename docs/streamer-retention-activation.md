# Activación del historial y los clics de streamer

Las solicitudes pendientes guardan `streamerHistoryDeleteAt` a 24 horas del envío. Al aprobar una publicación se retira el vencimiento: una emisión activa no se elimina por antigüedad. Al finalizarla se fija un plazo de siete días desde `endedAtMillis`, igual para el historial y el contador. Los rechazos manuales conservan siete días desde la revisión; los rechazos automáticos de tres horas conservan el plazo de 24 horas desde el envío. Los permisos validan el plazo y no permiten prolongarlo ni alterar el recuento al finalizar. La interfaz y el trabajo local aplican los mismos límites.

Con una identidad de despliegue configurada para el proyecto de Coach, ejecutar desde la raíz:

```sh
bash tools/activate-streamer-retention.sh wild-rift-drafting
```

El script publica las reglas y habilita TTL exclusivamente sobre `streamerHistoryDeleteAt` en los dos grupos correspondientes. No cambia otros índices ni abre una sesión interactiva. TTL elimina en segundo plano y puede retrasar la eliminación física; la visibilidad termina al vencer el plazo. Los registros antiguos sin ese campo siguen cubiertos por la limpieza local.

En esta sesión no hay identidad de despliegue disponible, por lo que no se afirma que estas reglas o políticas TTL estén activadas. La publicación del APK no despliega estos permisos. Los contadores no disponibles se muestran como tales y no se inventan cifras.

El respaldo por cuenta en el dispositivo combina publicaciones por identificador y conserva las anteriores al enviar una nueva. Este respaldo no sustituye la activación del historial compartido ni proporciona estadísticas de otros dispositivos.
