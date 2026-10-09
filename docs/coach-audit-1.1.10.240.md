# Coach 1.1.10.240 — código 956

## Cambios

- Cada runa situacional conserva al abrirse el nombre, icono y explicación de la opción pulsada. Las coincidencias flexibles del catálogo ya no pueden convertir dos alternativas distintas en el mismo detalle.
- Los controles de runas principales y situacionales usan identificadores únicos, lo que permite verificar cada alternativa de forma independiente.
- La dependencia de anotaciones usada por las pruebas instrumentadas se fija en la versión 2.26.0, la misma que exige estrictamente la aplicación. Así se evita el conflicto con la versión 2.30.0 solicitada por Espresso.
- Todas las configuraciones instrumentadas fuerzan esa misma versión para que la resolución también se aplique a lint.
- El trabajador económico mantiene el límite de 30 solicitudes si Firestore aún está creando sus índices, y el flujo de permisos conserva el trabajador programado cuando Cloud Functions no puede desplegarse por permisos.

## Verificación

- Revisión del flujo de detalle de runas, incluidas las entradas personalizadas y las alternativas del catálogo.
- Las comprobaciones de compilación y APK se ejecutarán en la entrega automática, incluida la resolución de dependencias de pruebas.

## Resumen para testers

Coach 1.1.10.240: abre cada runa situacional de una build que tenga varias alternativas. Cada una debe mostrar su propio nombre, icono y explicación, sin repetir el detalle de otra runa.
