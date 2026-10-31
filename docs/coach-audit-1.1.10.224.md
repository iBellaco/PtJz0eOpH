# Coach 1.1.10.224 — código 940

## Alcance

- Auditoría del índice y del historial completo: no hay objetos versionados con extensión `.keystore`, `.jks` o `.base64`, ni `debug.keystore.base64`. Se conserva la firma persistente introducida en Coach 164. Las configuraciones históricas de Supabase permanecen en el historial; Supabase ya no se utiliza. No se ha reescrito el historial ni se afirma una rotación de credenciales externas.
- Exclusiones globales para claves, base64, `secrets.properties` y credenciales temporales; defaults públicos. Se elimina la restauración desde un archivo base64 del repositorio. El índice actual no conserva archivos que ya coincidan con exclusiones; se retira el marcador de conexión de la JVM y la copia obsoleta de app/applet, y se reubican generadores anteriores en tools/legacy.
- Wrapper oficial Gradle 9.3.1 completo. JAR SHA256 `b3a875ddc1f044746e1b1a55f645584505f4a10438c1afea9f15e92a7c42ec13`; distribución con suma oficial configurada. CI deja de borrarlo.
- Administración autorizada por claim firmado; desaparecen el correo y los perfiles/marcos como fuentes de autorización de isAdmin. Los marcos tampoco confieren moderación ni permiso de streamer. La migración del titular es explícita, verificada y conserva otros claims.
- Compras, canjes, resoluciones, ajustes, tiempo Premium, suscripciones a creadores y anuncios pasan a transacciones del servidor. El cliente no puede escribir saldos ni comprobantes, tampoco desde una cuenta de staff. El reloj, precio, elegibilidad y comisiones se comprueban en el servidor; los reintentos son idempotentes y un rechazo no devuelve saldo dos veces.
- Pruebas de reglas, servicio, matchup y scraper obligatorias antes de compilar el APK; continúan los controles de pantallas, R8, firma e instalación. Los scripts Python ejecutan tests Kotlin reales con fixtures, en lugar de un request externo que ocultaba errores.
- Entrega automática de dos ZIP públicos, con comprobación de sumas y archivo fuente obtenido mediante git archive. No se incluyen archivos privados ni APK en el ZIP del proyecto.
- AGENTS.md es la fuente única de instrucciones; INSTRUCCIONES_AGENTES.md conserva el enlace de compatibilidad. README distingue el paquete com.Coach del namespace com.example, describe el wrapper y la activación efectiva. Las políticas ya existentes siguen requiriendo revisión especializada de la monetización, permisos de terceros y obligaciones locales.

## Validación y límites

La cuenta del titular indicada por el usuario ya recibió un claim firmado, preservando los anteriores. El proyecto no puede activar Cloud Build sin un plan de pago, y el titular confirmó que no dispone de él. Se usa por ello una cola privada y el procesador de servidor existente, programado cada cinco minutos mediante el repositorio público. No se activa ningún servicio facturado. Las reglas impiden reemplazar una solicitud en espera, fabricar resultados o leer solicitudes ajenas. El servidor consulta claims actuales y sesiones revocadas; el lease y los movimientos idempotentes permiten recuperarse de una interrupción. La activación real pasó en la ejecución 37800256846: procesador verificado, claims conservados y reglas publicadas/leídas de vuelta con SHA256 2afe7a1f4d3d6248a4d41bacd5411d68ae8b4daf8fd76aff5ff06e5288a6843c. El horario permanente se incorpora al fusionar en main. El APK y los ZIP aún deben superar los controles de esta fuente antes de entregarse.

Validación local completada: 31 pruebas de política, 53 escenarios de reglas y 19 casos de economía en emuladores, además de la integración de borrado de cuentas y 30 comprobaciones de scripts de entrega. Las pruebas de política y operaciones se ejecutan en el proyecto aislado demo-coach-tests; incluyen los cuatro planes/precios, carreras de saldo, replay, cambios de comisión, destinos USDT, claims falsos, suspensión/eliminación, pagos a creadores, anuncios, reembolso a los siete días, conservación de solicitudes pendientes y limpieza de membresías al borrar una cuenta. Los resultados de compilación, instalación y publicación corresponden a las ejecuciones de esta versión y deben comprobarse antes de entregar. No se declara activo el procesador por tener código o tests de emulator.

El intento de cambiar la descripción pública del repositorio fue rechazado con HTTP 403 por la conexión de GitHub. El propietario debe sustituirla desde Settings; descripción propuesta: «Coach: aplicación Android de asistencia al draft de Wild Rift, con reconocimiento local y contenido en español y portugués».

La revisión legal especializada no está certificada. La guía docs/economy-operations.md enumera las decisiones pendientes y el procedimiento de activación sin bloquear el acceso previo del titular si falla el despliegue.

## Resumen para testers

Coach 1.1.10.224: comprobar compras, canjes, reintentos y reembolsos sin movimientos duplicados. Un error de conexión no debe anunciar éxito ni modificar el saldo local. Revisar suscripciones a creadores, envío de anuncios, español y portugués. Descargar los dos ZIP: uno con el APK listo para instalar y otro con el proyecto.

## Commit copiable

fix: proteger cuentas y operaciones de esencias y publicar ZIP descargables de Coach
