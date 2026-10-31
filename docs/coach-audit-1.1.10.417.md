# Coach 1.1.10.417 — build 1133

## Causa y cambio autorizado

Las publicaciones de patrocinio que no alcanzaban el servicio directo usaban
una única entrada de cola por usuario. Una solicitud pendiente bloqueaba las
siguientes y, mientras la cola no terminaba, no se creaba el anuncio pendiente
que Moderación utiliza para mostrar la aprobación.

Cada publicación de patrocinio ahora usa su propio identificador de solicitud.
El trabajador conserva compatibilidad con las entradas antiguas y entrega el
resultado al propietario correcto. El botón Nuevo Anuncio ya no se desactiva
por la antigua solicitud única, por lo que pueden enviarse varias publicaciones
independientes. La aprobación continúa centralizada en Moderación mediante el
registro pendiente del anuncio.

## Archivos

- app/build.gradle.kts.
- app/src/main/java/com/example/data/EconomyServiceClient.kt.
- app/src/main/java/com/example/ui/components/SponsorCpmPanelDialog.kt.
- firestore.rules.
- server/economy/queue.mjs.
- server/economy/test/emulator.integration.mjs.
- docs/coach-audit-1.1.10.417.md.

## Verificaciones

La integración del servicio económico pasó 27 casos, incluido un escenario
con dos publicaciones independientes que ambas llegan a Moderación y cobran
una sola vez. Las reglas pasaron 64 escenarios. `git diff --check` no reporta
errores.

No hay SDK Android local y no se ejecutó una nueva compilación Android. El
cambio queda únicamente en fix-error; coach-validacion y main no se modifican.
Se espera el aviso del propietario para promover, sin monitoreo continuo.

Resumen para testers: Coach 1.1.10.417 permite enviar varias publicaciones de
patrocinio y conserva cada una como solicitud independiente para que aparezca
en Moderación y pueda aprobarse.

Commit: Separar solicitudes de patrocinio para moderación
