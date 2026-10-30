# Coach 1.1.10.418 — build 1134

## Causa y corrección

La cuenta administradora ya tenía el permiso firmado, pero el despliegue de la
función directa continúa limitado por permisos de Google Cloud. El cliente
rechazaba las operaciones administrativas antes de enviarlas al procesador
privado, por lo que Dar esencia mostraba un error genérico aunque el trabajador
confiable sí estuviera activo.

Las operaciones administrativas autorizadas ahora pueden usar esa cola privada
como respaldo cuando el transporte directo no está disponible. El servidor
vuelve a validar el claim firmado, la cuenta, la sesión, el saldo y la
operación antes de ejecutar; el cliente nunca escribe saldos, roles ni recibos.
La operación CLEANUP continúa excluida del envío desde la aplicación.

## Archivos

- app/build.gradle.kts.
- app/src/main/java/com/example/data/EconomyRequestPolicy.kt.
- app/src/test/java/com/example/EconomyRequestPolicyTest.kt.
- docs/coach-audit-1.1.10.418.md.

## Verificaciones

Se comprobó en el flujo de permisos que la identidad administradora quedó
validada y que el trabajador privado está activo. El despliegue de la función
directa sigue pendiente por un HTTP 403 de Google Cloud; esta corrección usa el
procesador alternativo autorizado. La prueba unitaria confirma que los
comandos administrativos pueden encolarse y que CLEANUP no puede enviarse
desde la aplicación.

No hay SDK Android local y no se ejecutó una nueva compilación Android. El
cambio comienza únicamente en fix-error; main no se modifica manualmente.

Resumen para testers: Coach 1.1.10.418 permite que el administrador entregue
esencias y gestione operaciones mediante el procesador privado cuando el
transporte directo no está disponible, sin relajar la autorización del servidor.

Commit: Permitir respaldo privado para operaciones administrativas
