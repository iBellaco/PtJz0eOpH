# Coach 1.1.10.414 — build 1130

## Fallo comprobado

La validación 40805745634 del commit f6d66f5c falló en core-and-rendered
(trabajo 123225019329): WildRiftItemsCatalogTest, método
`item stats match official in-game properties`, línea 157. Se ejecutaron 812
pruebas y falló una. La expectativa todavía exigía +8% de poder de curaciones
y escudos para Diadema melodiosa, mientras el catálogo ya contiene +6%.

Se volvió a consultar la fuente oficial proporcionada por el propietario:
https://wildrift.leagueoflegends.com/es-es/news/game-updates/wild-rift-patch-notes-7-3d/
Confirma el cambio de 8% a 6% tanto en Diadema susurrante como en Diadema
melodiosa. No corresponde revertir el catálogo para satisfacer la prueba vieja.

El escáner, seguridad, compilación release, auditorías instaladas española y
portuguesa, preflight nativo, las 16 particiones de secuencias y su auditoría
final pasaron. La entrega y el informe final quedaron rojos como consecuencia
del fallo unitario; no se presenta ese APK como una entrega validada.

## Corrección y alcance

- Actualizar exclusivamente la expectativa de Diadema melodiosa a +6%, con
  mensaje de error que incluye las estadísticas recibidas.
- Conservar todas las demás comprobaciones del método y de la clase, incluidas
  las pasivas 0,25% y 0,8%, que son valores distintos y no cambiaron.
- Incluir toda WildRiftItemsCatalogTest en el trabajo obligatorio de fix-error,
  junto con CatalogBalanceTest, que ya verifica ambos objetos en español y
  portugués. No se desactiva ni se omite ninguna prueba.
- No modificar catálogos productivos, reconocimiento, roles, patrocinio,
  firma, trabajos instalados ni el flujo de validación completa.

## Archivos modificados

- app/src/test/java/com/example/WildRiftItemsCatalogTest.kt.
- .github/workflows/fix-error.yml.
- app/build.gradle.kts.
- docs/coach-audit-1.1.10.414.md.

## Verificaciones

Se compilaron los modelos y catálogos productivos junto con las dos clases de
pruebas en Kotlin/JUnit. Se usaron adaptadores mínimos para recursos, Color,
anotación Serializable y traducción; no sustituye una ejecución Android.
Antes del cambio: 14 pruebas, exactamente el fallo de la línea 157.
Después: las 14 pruebas pasaron. No se alteraron datos productivos entre ambas.

Pasaron las validaciones de recursos ES/PT, contenido y builds generadas.
La comparación estructural YAML confirma que el único cambio de flujo es
añadir la clase completa de catálogo al conjunto obligatorio existente.
git diff --check sin errores.

No hay SDK Android local. La nueva ejecución de Actions debe confirmar la
integración Android antes de promover. Solo se publica en fix-error; se espera
el aviso del propietario, sin monitoreo continuo. Coach-validacion y main no
se modifican en esta intervención.

Resumen para testers: Coach 1.1.10.414 corrige una comprobación desactualizada
del catálogo y la ejecuta desde la etapa fix. Diadema melodiosa conserva el 6%
de poder de curaciones y escudos. Sin cambios en el funcionamiento de la app.
Pendiente de validación Android.

Commit: Corregir la prueba de Diadema melodiosa y verificar el catálogo completo en fix
