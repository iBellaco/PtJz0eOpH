# Coach 1.1.10.236 — código 952

## Cambios

- Cada runa situacional conserva al abrirse el nombre, icono y explicación de la opción pulsada. Las coincidencias flexibles del catálogo ya no pueden convertir dos alternativas distintas en el mismo detalle.
- Los controles de runas principales y situacionales usan identificadores únicos, lo que permite verificar cada alternativa de forma independiente.
- La comprobación Android del flujo de publicación renueva sus dependencias antes de lint, para evitar falsos fallos por una respuesta antigua del caché de Maven.

## Verificación

- Revisión del flujo de detalle de runas, incluidas las entradas personalizadas y las alternativas del catálogo.
- Las comprobaciones de compilación y APK se ejecutarán en la entrega automática, incluida la descarga limpia de dependencias antes de lint.

## Resumen para testers

Coach 1.1.10.236: abre cada runa situacional de una build que tenga varias alternativas. Cada una debe mostrar su propio nombre, icono y explicación, sin repetir el detalle de otra runa.
