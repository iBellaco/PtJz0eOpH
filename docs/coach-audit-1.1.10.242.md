# Coach 1.1.10.242 — código 958

## Cambios

- Cada runa situacional conserva al abrirse el nombre, icono y explicación de la opción pulsada. Las coincidencias flexibles del catálogo ya no pueden convertir dos alternativas distintas en el mismo detalle.
- Las runas principales conservan la etiqueta que usan las pruebas de pantallas; cada runa situacional mantiene un identificador propio.
- La dependencia de anotaciones usada por las pruebas instrumentadas se fija en la versión 2.26.0, la misma que exige estrictamente la aplicación. Así se evita el conflicto con la versión 2.30.0 solicitada por Espresso.
- Espresso se ajusta a la versión compatible con las anotaciones ya fijadas por la aplicación, evitando que lint solicite una versión conflictiva.
- El trabajador económico mantiene el límite de 30 solicitudes si Firestore aún está creando sus índices, y el flujo de permisos conserva el trabajador programado cuando Cloud Functions no puede desplegarse por permisos.

## Verificación

- Revisión del flujo de detalle de runas, incluidas las entradas personalizadas y las alternativas del catálogo.
- El flujo anterior superó lint y compilación, pero falló en tres pruebas de pantallas que buscaban la etiqueta original de runas principales. La nueva ejecución comprobará esas pruebas y el APK.

## Resumen para testers

Coach 1.1.10.242: abre cada runa situacional de una build que tenga varias alternativas. Cada una debe mostrar su propio nombre, icono y explicación, sin repetir el detalle de otra runa.
