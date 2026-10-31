# Coach 1.1.10.416 — build 1132

## Causa y cambio autorizado

La ejecución 40867371892 terminó cancelada por el trabajo core-tests
(123426459055). GitHub confirmó que superó el límite de una hora.
Lint pasó en 7m48s y la suite unitaria completa pasó en 51m43s.
El reporte unitario se guardó antes del cierre; el trabajo cruzó su límite
durante los pasos finales de caché y limpieza. La revisión visual independiente
pasó en unos diez minutos. El resto de comprobaciones de aplicación también
pasó; publicación e informe final quedaron bloqueados por la cancelación.

El propietario autorizó ampliar únicamente core-tests de 60 a 75 minutos.
Se mantiene el límite finito y se reserva margen para preparación, lint,
suite completa y cierre de Gradle. No implica que cada ejecución dure 75 minutos
ni garantiza que cualquier ejecución futura termine dentro del nuevo límite.

La revisión visual y el escáner conservan sus 60 minutos. No se modifican
comandos, clases de prueba, dependencias, condiciones de publicación, firma
ni comportamiento de la aplicación. Se corrige un comentario del escáner
que ya no debe indicar que comparte el mismo presupuesto de core-tests.

## Archivos

- .github/workflows/build-apk.yml.
- .github/scripts/test_parallel_validation.py.
- app/build.gradle.kts.
- docs/coach-audit-1.1.10.416.md.

## Verificaciones

Pasaron 28 pruebas Python: 10 del flujo paralelo, 14 de reutilización y
4 del informe de fallos. La prueba de presupuestos exige exactamente 75
minutos para core-tests y 60 para rendered-audit.
La comparación del YAML resuelto confirma que el único cambio operativo
del flujo es core-tests.timeout-minutes: 60 → 75.
Recursos ES/PT, contenido y git diff --check aprobados.

No hay SDK Android local y no se ejecutó una nueva compilación Android.
Las pruebas del flujo ya están incluidas en el preflight obligatorio de fix.
Solo se publica en fix-error; coach-validacion y main permanecen intactas.
Se espera el aviso del propietario para promover, sin monitoreo continuo.

Resumen para testers: Coach 1.1.10.416 amplía el margen de la validación
unitaria para completar el guardado y cierre sin cancelación por el límite
anterior. Todas las pruebas siguen siendo obligatorias. Sin cambios funcionales
en la aplicación; pendiente de validación completa.

Commit: Ampliar a 75 minutos el margen de cierre de las pruebas unitarias
