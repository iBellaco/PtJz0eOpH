# Coach 1.1.10.237 — código 953

## Cambios

- Cada runa situacional conserva al abrirse el nombre, icono y explicación de la opción pulsada. Las coincidencias flexibles del catálogo ya no pueden convertir dos alternativas distintas en el mismo detalle.
- Los controles de runas principales y situacionales usan identificadores únicos, lo que permite verificar cada alternativa de forma independiente.
- La dependencia de anotaciones usada por las pruebas instrumentadas se fija en la versión 2.31.0, disponible en Maven, para evitar el falso fallo de caché con la versión 2.30.0.

## Verificación

- Revisión del flujo de detalle de runas, incluidas las entradas personalizadas y las alternativas del catálogo.
- Las comprobaciones de compilación y APK se ejecutarán en la entrega automática, incluida la resolución de dependencias de pruebas.

## Resumen para testers

Coach 1.1.10.237: abre cada runa situacional de una build que tenga varias alternativas. Cada una debe mostrar su propio nombre, icono y explicación, sin repetir el detalle de otra runa.
