# Coach 1.1.10.419 — build 1135

## Regla de conservación de flujos

La limpieza de GitHub Actions ya no reparte un límite global entre entregas y
servicios. Cada flujo conserva como mínimo 10 ejecuciones propias. Cuando el
flujo supera ese número, se eliminan únicamente sus ejecuciones completadas
más antiguas; no se eliminan ejecuciones activas ni se usan ejecuciones de otro
flujo para completar el mínimo. Se mantienen hasta 30 releases finales y el
borrador de firma protegido.

La misma limpieza se ejecuta después de los flujos de entrega, permisos,
economía, borrado de cuentas y corrección. Se agregó la limpieza que faltaba
en `fix-error` y se le concedió únicamente el permiso de acciones necesario
para retirar historiales completados.

## Archivos

- AGENTS.md.
- .github/scripts/cleanup-completed-runs.cjs.
- .github/scripts/cleanup-completed-runs.test.cjs.
- .github/workflows/account-deletion.yml.
- .github/workflows/build-apk.yml.
- .github/workflows/economy.yml.
- .github/workflows/firestore-rules.yml.
- .github/workflows/fix-error.yml.
- app/build.gradle.kts.
- docs/coach-audit-1.1.10.419.md.

## Verificaciones

La prueba de retención confirma que 11 ejecuciones de un flujo conservan las
10 más recientes, que otro flujo se limpia de forma independiente y que un
historial corto no se modifica. Los cinco archivos YAML pasan análisis de
sintaxis y `git diff --check` no reporta errores.

No hay SDK Android local y no se ejecutó una nueva compilación Android. La
regla comienza en `fix-error`; `coach-validacion` y `main` no se modifican.

Resumen para testers: Coach 1.1.10.419 conserva al menos 10 ejecuciones por
cada flujo automatizado y elimina solo el historial más antiguo del mismo
flujo cuando supera ese límite.

Commit: Conservar diez ejecuciones por cada flujo de trabajo
