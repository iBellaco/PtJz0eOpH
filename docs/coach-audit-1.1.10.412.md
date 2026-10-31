# Coach 1.1.10.412 — build 1128

## Diagnóstico comprobado

La validación 40770466398 del commit 9253cfdb terminó como cancelada, pero
contiene dos incidencias independientes:

- scanner-tests, trabajo 123106409179: GitHub registra «The job has exceeded
  the maximum execution time of 30m0s». La cancelación ocurrió durante la
  matriz completa de capturas; las regresiones previas habían aprobado.
- core-and-rendered, trabajo 123107341669: PortugueseGeneratedSurfaceTest
  detectó español en el consejo generado de Dardos rastreademonios. Se habían
  ejecutado 510 pruebas, con una fallida y una omitida. No se atribuye este fallo
  al escáner, a los permisos ni a un problema de reconocimiento de Vi.

Los trabajos de catálogos, seguridad, solicitudes y servicios finalizaron
correctamente. No se produjo un APK validado.

## Corrección

SituationalItemAdvisor usaba los campos españoles passive, stats y name cuando
no existía un consejo específico. Aunque el objeto ya tenía traducción portuguesa
completa, la traducción genérica podía hacer una sustitución parcial y devolver
una mezcla de idiomas. El consejo usa ahora getLocalizedPassive,
getLocalizedStats y getLocalizedName antes de traducir los textos genéricos.
Se mantienen las cifras actuales del objeto y los consejos específicos existentes.

Se añade una regresión ES → PT → ES que comprueba nombre, pasiva y estadísticas
contra el catálogo, los valores 85% y 17,5%, y la ausencia de español en portugués.
La clase completa PortugueseGeneratedSurfaceTest pasa a ejecutarse en fix-error,
incluida la prueba que falló, con conservación de sus informes de texto generado.

El presupuesto de scanner-tests sube de 30 a 60 minutos, igual que el de
core-and-rendered. Ese trabajo agrupa compilación, regresiones enfocadas y la
matriz completa. No se suprime ninguna prueba ni se cambia la concurrencia,
los requisitos de aprobación o el paso manual a main. Esto evita repetir el
corte a los 30 minutos; la duración final de la siguiente ejecución aún debe
comprobarse y no se promete que termine dentro del nuevo límite.

## Archivos modificados

- app/src/main/java/com/example/data/SituationalItemAdvisor.kt.
- app/src/test/java/com/example/PortugueseGeneratedSurfaceTest.kt.
- .github/workflows/fix-error.yml.
- .github/workflows/build-apk.yml.
- app/build.gradle.kts.
- docs/coach-audit-1.1.10.412.md.

## Verificación y límites

- Se reprodujo localmente la pasiva parcialmente traducida antes del cambio.
- Se compiló la lógica Kotlin real de consejos, objetos y catálogo de traducción
  con adaptadores mínimos de dependencias. La pasiva, nombre y estadísticas
  conservaron ES/PT/ES y sus valores. Los textos de los 179 consejos de objetos
  pasaron el patrón de residuos españoles aplicando el mismo paso de traducción
  final de la auditoría. No es una prueba de interfaz Android.
- Auditoría de fuentes en portugués: 2617 literales visibles, cero hallazgos.
- Validaciones de recursos ES/PT, contenido y builds generadas: correctas.
- Comparación estructural de build-apk.yml contra el commit anterior: idéntico
  salvo timeout-minutes de scanner-tests. Se conservan todos los trabajos,
  comandos y condiciones.
- git diff --check: correcto.

Las pruebas Robolectric nuevas y la compilación Android se ejecutarán en Actions;
no hay SDK Android local ni APK nuevo validado. Se conserva todo el trabajo de
baneos, roles, última selección, catálogo y patrocinio.

## Entrega

Solo fix-error. Esperar el aviso del propietario y comprobar el verde antes de
promover este mismo código a coach-validacion. Main queda bajo control manual
del propietario. No se mantiene monitoreo continuo.

Resumen para testers: Coach 1.1.10.412 corrige un consejo que mezclaba español
y portugués al consultar Dardos rastreademonios. Comprobar ES/PT/ES y que las
cifras del objeto se conserven. La validación del escáner dispone de más tiempo
sin reducir sus pruebas. Pendiente de validación Android.

Commit: Corregir consejos en portugués y ampliar el tiempo de auditoría del escáner
