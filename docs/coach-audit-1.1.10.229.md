# Coach 1.1.10.229 — código 945

## Cambios

- `server/economy/queue.mjs`, `test/queue-fallback.test.mjs`: si faltan permisos para crear índices, la cola lee los grupos de solicitudes pendientes y recuperables con los índices individuales, los ordena por antigüedad y procesa como máximo 30 operaciones por ejecución. La revisión manual conserva los identificadores originales. La prueba comprueba antigüedad y vencimiento del lease sin el índice compuesto.
- `.github/workflows/firestore-rules.yml`: un error 403 al crear índices queda registrado como advertencia y permite publicar las reglas verificadas. Otros errores siguen deteniendo el despliegue.
- `docs/economy-operations.md`: se documenta la limitación actual de permisos y el mayor consumo posible de lecturas de la consulta de respaldo.
- `app/build.gradle.kts`, `README.md`: versión 1.1.10.229, código 945.

## Verificación

La entrega 1.1.10.228 pasó permisos, economía, lint, pruebas Android, ofuscación, firma y ejecución instalada en español y portugués. Sus dos ZIP se descargaron desde los enlaces públicos y pasaron SHA-256 y prueba de integridad ZIP. El despliegue de índices recibió HTTP 403 y dejó sin ejecutar la publicación de reglas; esta versión corrige esa secuencia. Localmente pasan las 44 pruebas unitarias de economía, 56 escenarios de reglas, 24 casos de economía en emuladores y la validación YAML. La compilación y el despliegue de esta versión se comprueban en Actions antes de la entrega.

## Resumen para testers

Coach 1.1.10.229: comprobar que las solicitudes antiguas se atienden antes que las nuevas, que se muestran los estados de retraso y revisión, y que los permisos de cuentas suspendidas se aplican. Verificar el APK instalado en español y portugués y la versión visible.
