# Coach 1.1.10.415 — build 1131

## Causa confirmada y autorización

El propietario autorizó separar las pruebas unitarias y la revisión visual
en trabajos paralelos, primero en fix-error, sin promover todavía a validación.

En la ejecución 40820255576, el trabajo core-and-rendered (123271652899)
terminó cancelado. La anotación de GitHub indica explícitamente:
`The job has exceeded the maximum execution time of 1h0m0s`.
Lint pasó en unos seis minutos; la suite unitaria completa pasó en 48m55s.
La revisión visual portuguesa empezó a las 05:51:51 UTC y fue cancelada
a las 05:56:46 UTC, al consumir el presupuesto compartido. No fue una
cancelación manual ni un fallo de la prueba de Diadema melodiosa.

El escáner, seguridad, APK release, auditorías instaladas, preflight nativo,
las 16 particiones OCR y su auditoría final pasaron en esa ejecución.
La revisión visual incompleta sigue impidiendo considerar validada la entrega.

## Cambio

- core-tests conserva lint, la suite unitaria completa sin filtros, sus
  informes y un diagnóstico seguro de fallos. Mantiene el límite de 60 minutos.
- rendered-audit usa un ejecutor independiente con la misma preparación y
  su propio límite de 60 minutos. Conserva exactamente el comando de las 14
  clases visuales, --rerun, la grabación de imágenes y todos sus artefactos.
- Ambos dependen de la misma comprobación de seguridad y búsqueda de APK
  validado, no uno del otro. Pueden ejecutarse en paralelo según la capacidad
  de GitHub; no se promete una duración exacta ni se elimina el límite temporal.
- La publicación exige CORE_RESULT y RENDERED_RESULT satisfactorios.
  La reutilización exige también ambos nombres de trabajo en verde; no acepta
  una validación antigua que carezca de cualquiera de ellos.
- El informe final y la limpieza esperan a ambos trabajos. Se conservan los
  nombres de los artefactos, sin colisiones entre los dos ejecutores.
- fix-error y la comprobación previa de validación ejecutan las pruebas del
  nuevo grafo y de reutilización. PyYAML 6.0.3 se instala explícitamente para
  resolver anclas y comprobar el YAML real, no una aproximación por texto.

No se cambian los algoritmos, catálogos, patrocinio, firma, pruebas instaladas
ni los otros trabajos Android. No se ejecuta la validación completa desde fix.

## Archivos modificados

- .github/workflows/build-apk.yml.
- .github/workflows/fix-error.yml.
- .github/scripts/find-validated-apk.py.
- .github/scripts/test_validated_apk.py.
- .github/scripts/test_parallel_validation.py.
- app/build.gradle.kts.
- docs/coach-audit-1.1.10.415.md.

## Verificaciones locales

50 pruebas Python aprobadas: 10 de paralelización, 14 de reutilización,
4 del informe de fallos, 1 del reporte JUnit, 1 de ofuscación y 20 de políticas
de firma. Las pruebas de paralelización ejecutan el código real de la barrera
de publicación con estados correctos, fallidos, cancelados, omitidos y vacíos;
comprueban dependencias, preparación, límites, informes y todas las clases.

Se compararon los YAML resueltos con la versión anterior: los comandos y
artefactos de lint, suite completa y revisión visual son idénticos; se añade
el diagnóstico independiente del trabajo unitario. Los trabajos de escáner,
seguridad, compilación, pruebas instaladas y los trabajos Android/patrocinio
de fix-error permanecen estructuralmente iguales.

Pasaron recursos ES/PT, contenido, comprobación de builds generadas y
git diff --check. No hay SDK Android local; estas verificaciones no equivalen
a una nueva compilación o a ejecutar las pantallas. Se requiere el verde de
Actions en fix antes de promover, y después el de validación completa.

## Entrega

Se publica únicamente en fix-error. Coach-validacion y main permanecen
intactas. No se entrega un APK como validado ni se deja monitoreo continuo;
se espera el aviso del propietario.

Resumen para testers: Coach 1.1.10.415 separa las pruebas generales y visuales
para que no compartan el límite de tiempo. Ambas siguen siendo obligatorias.
Sin cambios funcionales en la aplicación; pendiente de validación completa.

Commit: Separar pruebas unitarias y revisión visual en validaciones paralelas obligatorias
