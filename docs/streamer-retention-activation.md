# Activación del historial y los clics de streamer

Las nuevas publicaciones guardan `streamerHistoryDeleteAt` a siete días del envío; el contador usa el mismo vencimiento. El campo se limita a los registros de streamer y se valida para que el propietario no pueda prolongarlo. La interfaz oculta los registros a los siete días según la fecha autoritativa, y el trabajo local limpia los registros vencidos cuando la cuenta está disponible.

Con una identidad de despliegue configurada para el proyecto de Coach, ejecutar desde la raíz:

```sh
bash tools/activate-streamer-retention.sh wild-rift-drafting
```

El script publica las reglas y habilita TTL exclusivamente sobre `streamerHistoryDeleteAt` en los dos grupos correspondientes. No cambia otros índices ni abre una sesión interactiva. TTL elimina en segundo plano y puede retrasar la eliminación física; la visibilidad termina al vencer el plazo. Los registros antiguos sin ese campo siguen cubiertos por la limpieza local.

En esta sesión no hay identidad de despliegue disponible, por lo que no se afirma que estas reglas o políticas TTL estén activadas. La publicación del APK no despliega estos permisos. Los contadores no disponibles se muestran como tales y no se inventan cifras.
